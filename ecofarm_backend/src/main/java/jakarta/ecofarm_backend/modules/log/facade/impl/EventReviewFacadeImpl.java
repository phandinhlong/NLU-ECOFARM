package jakarta.ecofarm_backend.modules.log.facade.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.exception.BadRequestException;
import jakarta.ecofarm_backend.common.exception.EntityNotFoundException;
import jakarta.ecofarm_backend.common.exception.PermissionDeniedException;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.BatchRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.CultivationEventRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.OutboxEventRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.UserRepository;
import jakarta.ecofarm_backend.modules.log.EventHasher;
import jakarta.ecofarm_backend.modules.log.EventPayloadValidator;
import jakarta.ecofarm_backend.modules.log.EventRequests.AmendRequest;
import jakarta.ecofarm_backend.modules.log.EventRequests.RejectRequest;
import jakarta.ecofarm_backend.modules.log.EventStatus;
import jakarta.ecofarm_backend.modules.log.EventType;
import jakarta.ecofarm_backend.modules.log.EventWriteGuard;
import jakarta.ecofarm_backend.modules.log.facade.EventReviewFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Logic duyệt/đính chính sự kiện. Mỗi thao tác khóa dòng batch rồi dòng sự kiện (qua repository) và
 * chạy trong một transaction nên thay đổi trạng thái, sự kiện mới và dòng outbox cùng commit hoặc
 * cùng rollback. Duyệt thêm đúng một sự kiện EVENT_APPROVED tham chiếu hash gốc và không bao giờ sửa
 * sự kiện gốc; đính chính tạo sự kiện mới và đánh dấu sự kiện cũ là SUPERSEDED (giữ payload và hash cũ).
 */
@ApplicationScoped
public class EventReviewFacadeImpl implements EventReviewFacade {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final BatchRepository batchRepository;
    private final CultivationEventRepository cultivationEventRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final UserRepository userRepository;

    @Inject
    public EventReviewFacadeImpl(BatchRepository batchRepository,
            CultivationEventRepository cultivationEventRepository,
            OutboxEventRepository outboxEventRepository,
            UserRepository userRepository) {
        this.batchRepository = batchRepository;
        this.cultivationEventRepository = cultivationEventRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public BaseResponse<String> approve(Long eventId, boolean engineerOrAdmin) {
        requireEngineer(engineerOrAdmin);
        CultivationEvent event = lockBatchThenEvent(eventId);
        EventWriteGuard.requireBatchWritable(event.getBatch().getStatus());

        // Idempotent có điều kiện: nếu đã có EVENT_APPROVED và trạng thái hiện VẪN là APPROVED thì duyệt
        // lại là no-op. Nhưng nếu sau khi duyệt sự kiện đã bị đính chính (SUPERSEDED) hay từ chối thì
        // KHÔNG được ép lại APPROVED - đó là làm sống lại nội dung đã bị thay thế.
        long alreadyApproved = cultivationEventRepository.countApprovalsByAmendsEventId(
                eventId, EventType.EVENT_APPROVED.code());
        if (alreadyApproved > 0) {
            if (!EventStatus.APPROVED.name().equals(event.getStatus())) {
                throw new EventWriteGuard.ConflictException(
                        "Sự kiện đã rời trạng thái APPROVED, không thể duyệt lại: " + event.getStatus());
            }
            return BaseResponse.ok();
        }

        EventWriteGuard.requireApprovable(EventStatus.valueOf(event.getStatus()));
        int seq = cultivationEventRepository.nextSeq(event.getBatch().getId());
        // Payload đưa vào hash và payload lưu DB phải là CÙNG một chuỗi để hash xác minh lại được.
        String payloadJson = JSON.createObjectNode()
                .put("schemaVersion", 1).put("approvedEventHash", event.getEventHash()).toString();
        String hash = EventHasher.eventHash(readTree(payloadJson), List.of());
        CultivationEvent approval = CultivationEvent.builder()
                .batch(event.getBatch()).seq(seq).eventType(EventType.EVENT_APPROVED.code())
                .payloadJson(payloadJson)
                .eventHash(hash).status(EventStatus.PENDING.name())
                .recordedBy(event.getRecordedBy()).recordedAt(System.currentTimeMillis())
                .occurredAt(System.currentTimeMillis()).amendsEvent(event).build();
        cultivationEventRepository.persist(approval);
        enqueuePrivate(event.getBatch(), seq, hash);
        event.setStatus(EventStatus.APPROVED.name());
        return BaseResponse.ok();
    }

    @Override
    @Transactional
    public BaseResponse<String> reject(Long eventId, RejectRequest request, boolean engineerOrAdmin) {
        requireEngineer(engineerOrAdmin);
        if (request == null || request.reason() == null || request.reason().isBlank()) {
            throw new BadRequestException(ErrorCode.REJECT_REASON_REQUIRED);
        }
        CultivationEvent event = lockBatchThenEvent(eventId);
        EventWriteGuard.requireBatchWritable(event.getBatch().getStatus());
        EventWriteGuard.requireApprovable(EventStatus.valueOf(event.getStatus()));
        // Nội dung gốc giữ nguyên; chỉ trạng thái thay đổi.
        event.setStatus(EventStatus.REJECTED.name());
        return BaseResponse.ok();
    }

    @Override
    @Transactional
    public BaseResponse<String> amend(Long eventId, AmendRequest request, Long actorId, boolean engineerOrAdmin) {
        requireEngineer(engineerOrAdmin);
        if (request == null || request.payload() == null) {
            throw new BadRequestException(ErrorCode.INVALID_EVENT_PAYLOAD);
        }
        CultivationEvent prior = lockBatchThenEvent(eventId);
        // Ngoài vai trò ENGINEER, actor còn phải được phân công cho lô (phòng thủ nhiều lớp).
        requireAssignedToBatch(prior.getBatch().getId(), actorId);
        EventWriteGuard.requireBatchWritable(prior.getBatch().getStatus());
        EventWriteGuard.requireAmendable(EventStatus.valueOf(prior.getStatus()));
        EventType type = EventType.fromCode(prior.getEventType())
                .orElseThrow(() -> new IllegalStateException("Sự kiện đã lưu có loại không xác định"));
        EventPayloadValidator.validate(type, request.payload());

        int seq = cultivationEventRepository.nextSeq(prior.getBatch().getId());
        String hash = EventHasher.eventHash(request.payload(), List.of());
        CultivationEvent amended = CultivationEvent.builder()
                .batch(prior.getBatch()).seq(seq).eventType(prior.getEventType())
                .payloadJson(request.payload().toString()).eventHash(hash)
                .status(EventStatus.PENDING.name())
                .recordedBy(requireUser(actorId))
                .recordedAt(System.currentTimeMillis()).occurredAt(request.occurredAt())
                .clientEventId(request.clientEventId()).amendsEvent(prior).build();
        cultivationEventRepository.persist(amended);
        // Payload và hash của sự kiện cũ không đổi; chỉ trạng thái chuyển sang SUPERSEDED.
        prior.setStatus(EventStatus.SUPERSEDED.name());
        enqueuePrivate(prior.getBatch(), seq, hash);
        return BaseResponse.ok();
    }

    /**
     * Khóa dòng batch trước, rồi dòng sự kiện. Cùng thứ tự với create/harvest/QC (batch trước) nên
     * tránh deadlock, và khóa batch tuần tự hóa việc cấp seq chống đua trên unique (batch_id, seq).
     */
    private CultivationEvent lockBatchThenEvent(Long eventId) {
        CultivationEvent probe = cultivationEventRepository.findById(eventId);
        if (probe == null) throw new EntityNotFoundException(ErrorCode.EVENT_NOT_FOUND);
        batchRepository.findByIdForUpdate(probe.getBatch().getId());
        CultivationEvent event = cultivationEventRepository.findByIdForUpdate(eventId);
        if (event == null) throw new EntityNotFoundException(ErrorCode.EVENT_NOT_FOUND);
        return event;
    }

    private void requireEngineer(boolean engineerOrAdmin) {
        if (!engineerOrAdmin) throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
    }

    /** Đính chính chỉ cho người được phân công còn hiệu lực của lô, tránh sửa sự kiện của lô khác. */
    private void requireAssignedToBatch(Long batchId, Long actorId) {
        if (actorId == null) throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
        if (batchRepository.countActiveAssignment(batchId, actorId) == 0) {
            throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
        }
    }

    private com.fasterxml.jackson.databind.JsonNode readTree(String json) {
        try {
            return JSON.readTree(json);
        } catch (Exception e) {
            throw new IllegalStateException("Payload duyệt không hợp lệ", e);
        }
    }

    private User requireUser(Long actorId) {
        return userRepository.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    private void enqueuePrivate(Batch batch, int seq, String eventHash) {
        OutboxEvent outbox = OutboxEvent.builder()
                .batchIdHash(EventHasher.batchIdHash(batch.getId(), batch.getSaltEnc()))
                .seq(seq).eventData(eventHash).status(OutboxEvent.OutboxStatus.PENDING).attempts(0).build();
        outboxEventRepository.persist(outbox);
    }
}
