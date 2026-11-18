package jakarta.ecofarm_backend.modules.shipment.facade.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ecofarm_backend.common.dto.request.AuditLogParam;
import jakarta.ecofarm_backend.common.dto.request.ShipmentCreateRequest;
import jakarta.ecofarm_backend.common.dto.request.ShipmentReceiveRequest;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.ShipmentResponse;
import jakarta.ecofarm_backend.common.enums.BatchEvent;
import jakarta.ecofarm_backend.common.enums.BatchStatus;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.exception.ConflictException;
import jakarta.ecofarm_backend.common.exception.EntityNotFoundException;
import jakarta.ecofarm_backend.common.exception.PermissionDeniedException;
import jakarta.ecofarm_backend.common.util.CurrentUserUtil;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.AuditLog;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Shipment;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.AuditLogRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.BatchRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.ShipmentRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.UserRepository;
import jakarta.ecofarm_backend.infrastructure.security.UserSecurityDetailsPrincipal;
import jakarta.ecofarm_backend.modules.shipment.facade.ShipmentFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ShipmentFacadeImpl implements ShipmentFacade {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String STATUS_SHIPPED = "SHIPPED";
    private static final String STATUS_RECEIVED = "RECEIVED";
    private static final String STATUS_REJECTED = "REJECTED";

    @Inject
    private ShipmentRepository shipmentRepository;

    @Inject
    private BatchRepository batchRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private AuditLogRepository auditLogRepository;

    @Inject
    private CurrentUserUtil currentUser;

    @Inject
    private HttpServletRequest httpRequest;

    @Override
    @Transactional
    public BaseResponse<ShipmentResponse> createShipment(Long batchId, ShipmentCreateRequest request) {
        Long actorId = resolveActorId();
        String ipAddr = resolveClientIp();

        Batch batch = batchRepository.findById(batchId);
        if (batch == null) {
            throw new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND);
        }

        BatchStatus status = statusOf(batch);
        if (status == BatchStatus.RECALLED) {
            throw new ConflictException(ErrorCode.BATCH_NOT_SHIPPABLE, List.of("Lô đã bị thu hồi, cấm xuất kho"));
        }
        if (!status.canFire(BatchEvent.SHIP)) {
            throw new ConflictException(ErrorCode.BATCH_NOT_SHIPPABLE, List.of("Chỉ lô ở trạng thái ACTIVE mới được xuất kho"));
        }
        if (Boolean.TRUE.equals(shipmentRepository.existsShipmentInProgressForBatch(batchId))) {
            throw new ConflictException(ErrorCode.SHIPMENT_IN_PROGRESS_EXISTS, List.of("Lô đã có phiếu xuất kho đang giao, không thể tạo thêm"));
        }

        Shipment shipment = Shipment.builder()
                .batch(batch)
                .destination(request.getDestination().trim())
                .vehiclePlate(request.getVehiclePlate().trim())
                .shippedAt(request.getShippedAt() != null ? request.getShippedAt() : System.currentTimeMillis())
                .quantity(request.getQuantity())
                .status(STATUS_SHIPPED)
                .build();
        shipmentRepository.persist(shipment);

        batch.setStatus(status.fire(BatchEvent.SHIP).name());
        batchRepository.merge(batch);

        auditLogRepository.persist(buildAuditLog(AuditLogParam.builder()
                .actorId(actorId)
                .action("SHIPMENT_CREATED")
                .entity("shipments")
                .entityId(shipment.getId())
                .detail(toJson(Map.of(
                        "batchId", batchId,
                        "destination", shipment.getDestination(),
                        "vehiclePlate", shipment.getVehiclePlate(),
                        "quantity", shipment.getQuantity())))
                .ipAddr(ipAddr)
                .build()));

        return BaseResponse.build(toResponse(shipment));
    }

    @Override
    @Transactional
    public BaseResponse<ShipmentResponse> receiveShipment(Long shipmentId, ShipmentReceiveRequest request) {
        Long actorId = resolveActorId();
        String ipAddr = resolveClientIp();

        Shipment shipment = shipmentRepository.findById(shipmentId);
        if (shipment == null) {
            throw new EntityNotFoundException(ErrorCode.SHIPMENT_NOT_FOUND);
        }
        if (!STATUS_SHIPPED.equals(shipment.getStatus())) {
            throw new ConflictException(ErrorCode.SHIPMENT_ALREADY_PROCESSED, List.of("Phiếu xuất kho đã được xử lý trước đó"));
        }

        shipment.setReceivedAt(request.getReceivedAt() != null ? request.getReceivedAt() : System.currentTimeMillis());
        shipment.setReceivedBy(actorId);
        shipment.setConditionNote(trimToNull(request.getConditionNote()));
        shipment.setPhotoUrls(request.getPhotoUrls() == null || request.getPhotoUrls().isEmpty()
                ? null
                : toJson(request.getPhotoUrls()));

        boolean accepted = Boolean.TRUE.equals(request.getAccepted());
        String action;
        if (accepted) {
            shipment.setStatus(STATUS_RECEIVED);
            action = "SHIPMENT_RECEIVED";
        } else {
            if (shipment.getConditionNote() == null) {
                throw new ConflictException(ErrorCode.RECEIVE_REASON_REQUIRED, List.of("Từ chối nhận hàng phải kèm ghi chú lý do"));
            }
            shipment.setStatus(STATUS_REJECTED);
            action = "SHIPMENT_REJECTED";

            Batch batch = shipment.getBatch();
            if (batch != null) {
                BatchStatus current = statusOf(batch);
                batch.setStatus(current.canFire(BatchEvent.REJECT_DELIVERY)
                        ? current.fire(BatchEvent.REJECT_DELIVERY).name()
                        : BatchStatus.ACTIVE.name());
                batchRepository.merge(batch);
            }
        }
        shipmentRepository.merge(shipment);

        auditLogRepository.persist(buildAuditLog(AuditLogParam.builder()
                .actorId(actorId)
                .action(action)
                .entity("shipments")
                .entityId(shipmentId)
                .detail(toJson(Map.of(
                        "batchId", shipment.getBatch() != null ? shipment.getBatch().getId() : null,
                        "status", shipment.getStatus(),
                        "receivedAt", shipment.getReceivedAt())))
                .ipAddr(ipAddr)
                .build()));

        return BaseResponse.build(toResponse(shipment));
    }

    @Override
    public BaseResponse<List<ShipmentResponse>> findShipmentsByBatch(Long batchId) {
        Batch batch = batchRepository.findById(batchId);
        if (batch == null) {
            throw new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND);
        }
        List<ShipmentResponse> content = shipmentRepository.findByBatchId(batchId).stream()
                .map(this::toResponse)
                .toList();
        return BaseResponse.build(content);
    }

    @Override
    public BaseResponse<List<ShipmentResponse>> findShipmentsByStatus(String status) {
        String normalized = trimToNull(status);
        if (normalized != null) {
            normalized = normalized.toUpperCase();
        }
        List<ShipmentResponse> content = (normalized == null
                ? shipmentRepository.findAll()
                : shipmentRepository.findByStatus(normalized)).stream()
                .map(this::toResponse)
                .toList();
        return BaseResponse.build(content);
    }

    private ShipmentResponse toResponse(Shipment shipment) {
        Batch batch = shipment.getBatch();
        return ShipmentResponse.builder()
                .id(shipment.getId())
                .batchId(batch != null ? batch.getId() : null)
                .batchCode(batch != null ? batch.getBatchCode() : null)
                .destination(shipment.getDestination())
                .vehiclePlate(shipment.getVehiclePlate())
                .shippedAt(shipment.getShippedAt())
                .receivedAt(shipment.getReceivedAt())
                .receivedBy(shipment.getReceivedBy())
                .quantity(shipment.getQuantity())
                .status(shipment.getStatus())
                .conditionNote(shipment.getConditionNote())
                .photoUrls(parsePhotoUrls(shipment.getPhotoUrls()))
                .version(shipment.getVersion())
                .createdAt(shipment.getCreatedAt())
                .updatedAt(shipment.getUpdatedAt())
                .build();
    }

    private List<String> parsePhotoUrls(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return null;
        }
    }

    private Long resolveActorId() {
        UserSecurityDetailsPrincipal principal = currentUser.get();
        if (principal == null || principal.getId() == null) {
            throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
        }
        return principal.getId();
    }

    private String resolveClientIp() {
        if (httpRequest == null) {
            return null;
        }
        String forwarded = httpRequest.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return httpRequest.getRemoteAddr();
    }

    private static BatchStatus statusOf(Batch batch) {
        if (batch.getStatus() == null) {
            return BatchStatus.DRAFT;
        }
        try {
            return BatchStatus.valueOf(batch.getStatus());
        } catch (IllegalArgumentException e) {
            return BatchStatus.DRAFT;
        }
    }

    private AuditLog buildAuditLog(AuditLogParam param) {
        if (param == null) {
            return null;
        }
        return AuditLog.builder()
                .actor(param.getActorId() != null ? userRepository.findById(param.getActorId()).orElse(null) : null)
                .action(param.getAction())
                .entity(param.getEntity())
                .entityId(param.getEntityId())
                .detail(param.getDetail())
                .ipAddr(param.getIpAddr())
                .build();
    }

    private String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
