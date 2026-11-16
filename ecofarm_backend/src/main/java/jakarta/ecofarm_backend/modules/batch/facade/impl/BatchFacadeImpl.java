package jakarta.ecofarm_backend.modules.batch.facade.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ecofarm_backend.common.anotation.DatabaseTransactional;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.request.PageCriteria;
import jakarta.ecofarm_backend.common.dto.response.PageResponse;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.enums.RoleEnum;
import jakarta.ecofarm_backend.common.exception.ConflictException;
import jakarta.ecofarm_backend.common.exception.EntityNotFoundException;
import jakarta.ecofarm_backend.common.exception.PermissionDeniedException;
import jakarta.ecofarm_backend.common.util.CurrentUserUtil;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.AuditLog;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.BatchAssignment;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Greenhouse;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Variety;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.AuditLogRepository;
import jakarta.ecofarm_backend.infrastructure.service.BatchService;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.GreenhouseRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.UserRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.VarietyRepository;
import jakarta.ecofarm_backend.infrastructure.security.UserSecurityDetailsPrincipal;
import jakarta.ecofarm_backend.common.enums.BatchStatus;
import jakarta.ecofarm_backend.common.dto.request.AssignWorkersRequest;
import jakarta.ecofarm_backend.common.dto.response.BatchDetailResponse;
import jakarta.ecofarm_backend.common.dto.response.BatchListItemResponse;
import jakarta.ecofarm_backend.common.dto.request.CreateBatchRequest;
import jakarta.ecofarm_backend.common.dto.response.WorkerSummaryResponse;
import jakarta.ecofarm_backend.modules.batch.facade.BatchFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class BatchFacadeImpl implements BatchFacade {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final DateTimeFormatter BATCH_CODE_DATE = DateTimeFormatter.ofPattern("ddMMyy");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Inject
    private BatchService batchService;

    @Inject
    private GreenhouseRepository greenhouseRepository;

    @Inject
    private VarietyRepository varietyRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private AuditLogRepository auditLogRepository;

    @Inject
    private CurrentUserUtil currentUser;

    @Inject
    private HttpServletRequest httpRequest;

    @Override
    @DatabaseTransactional
    public BaseResponse<PageResponse<BatchListItemResponse>> list(int page, int size, String status,
            Long greenhouseId, String q, boolean mine) {
        Long actorId = resolveActorId();
        String statusFilter = status == null || status.isBlank() ? null : parseStatus(status).name();
        User actor = requireActor(actorId);
        Long mineUserId = mine && hasRole(actor, RoleEnum.FARMER) ? actorId : null;

        PageCriteria criteria = new PageCriteria(page, size, statusFilter, greenhouseId, q, mineUserId);
        List<Batch> batches = batchService.findPage(criteria);
        long total = batchService.countActive(criteria);

        List<Long> ids = batches.stream().map(Batch::getId).toList();
        Map<Long, Long> workerCounts = batchService.countAssignmentsByBatchIds(ids);
        Map<Long, Long> pollinationDates = batchService.findPollinationDates(ids);

        List<BatchListItemResponse> content = batches.stream()
                .map(b -> toListItem(b, workerCounts.getOrDefault(b.getId(), 0L), pollinationDates.get(b.getId())))
                .toList();

        return BaseResponse.build(new PageResponse<>(content, safePage(page), safeSize(size), total));
    }

    @Override
    @DatabaseTransactional
    public BaseResponse<BatchDetailResponse> detail(Long batchId) {
        Long actorId = resolveActorId();
        Batch batch = batchService.findById(batchId);
        if (batch == null) {
            throw new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND);
        }

        User actor = requireActor(actorId);
        if (hasRole(actor, RoleEnum.FARMER) && !batchService.isAssigned(batchId, actorId)) {
            throw new PermissionDeniedException(ErrorCode.BATCH_NOT_ASSIGNED);
        }

        return BaseResponse.build(toDetail(batch));
    }

    @Override
    @DatabaseTransactional
    public BaseResponse<BatchDetailResponse> create(CreateBatchRequest request) {
        Long actorId = resolveActorId();
        String ipAddr = resolveClientIp();
        User actor = requireEngineer(actorId);

        Greenhouse greenhouse = greenhouseRepository.findById(request.getGreenhouseId());
        if (greenhouse == null) {
            throw new EntityNotFoundException(ErrorCode.GREENHOUSE_NOT_FOUND);
        }

        Variety variety = varietyRepository.findById(request.getVarietyId());
        if (variety == null) {
            throw new EntityNotFoundException(ErrorCode.VARIETY_NOT_FOUND);
        }

        LocalDate sowDate;
        try {
            sowDate = LocalDate.parse(request.getSowDate());
        } catch (DateTimeParseException e) {
            throw new ConflictException(ErrorCode.INVALID_DATE);
        }

        Batch batch = Batch.builder()
                .batchCode(uniqueBatchCode(greenhouse.getCode(), sowDate))
                .greenhouse(greenhouse)
                .variety(variety)
                .status(BatchStatus.DRAFT.name())
                .sowDate(sowDate)
                .saltEnc(SECURE_RANDOM.nextLong())
                .createdBy(actor)
                .build();
        batchService.persist(batch);

        auditLogRepository.persist(buildAuditLog(actorId, "BATCH_CREATE", "batches", batch.getId(),
                toJson(Map.of("batchCode", batch.getBatchCode(), "status", batch.getStatus())), ipAddr));

        return BaseResponse.build(toDetail(batch));
    }

    @Override
    @DatabaseTransactional
    public BaseResponse<List<WorkerSummaryResponse>> listWorkers() {
        return BaseResponse.build(userRepository.findActiveUsersByRole(RoleEnum.FARMER).stream()
                .map(this::toWorker)
                .toList());
    }

    @Override
    @DatabaseTransactional
    public BaseResponse<List<WorkerSummaryResponse>> listAssignedWorkers(Long batchId) {
        Batch batch = batchService.findById(batchId);
        if (batch == null) {
            throw new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND);
        }
        return BaseResponse.build(batchService.findAssignments(batchId).stream()
                .map(BatchAssignment::getUser)
                .map(this::toWorker)
                .toList());
    }

    @Override
    @DatabaseTransactional
    public BaseResponse<List<WorkerSummaryResponse>> assignWorkers(Long batchId, AssignWorkersRequest request) {
        Long actorId = resolveActorId();
        String ipAddr = resolveClientIp();
        requireEngineer(actorId);

        Batch batch = batchService.findById(batchId);
        if (batch == null) {
            throw new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND);
        }
        if (statusOf(batch).isLocked()) {
            throw new ConflictException(ErrorCode.BATCH_LOCKED);
        }

        List<Long> userIds = request.getUserIds() == null ? List.of() : request.getUserIds().stream().distinct().toList();
        List<User> workers = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (Long userId : userIds) {
            User worker = userRepository.findById(userId).orElse(null);
            if (worker == null || !worker.isActive() || !hasRole(worker, RoleEnum.FARMER)) {
                errors.add("Người lao động " + userId + " không hợp lệ");
                continue;
            }
            workers.add(worker);
        }
        if (!errors.isEmpty()) {
            throw new ConflictException(ErrorCode.INVALID_WORKER, errors);
        }

        batchService.deleteAssignmentsByBatch(batchId);
        for (User worker : workers) {
            batchService.persistAssignment(BatchAssignment.builder().batch(batch).user(worker).build());
        }

        auditLogRepository.persist(buildAuditLog(actorId, "BATCH_ASSIGN_WORKERS", "batches", batchId,
                toJson(Map.of("userIds", workers.stream().map(User::getId).toList())), ipAddr));

        return BaseResponse.build(workers.stream().map(this::toWorker).toList());
    }

    private BatchDetailResponse toDetail(Batch batch) {
        Greenhouse greenhouse = batch.getGreenhouse();
        Variety variety = batch.getVariety();
        BatchStatus status = statusOf(batch);
        Long pollinationDate = batchService.findPollinationDate(batch.getId());
        List<BatchAssignment> assignments = batchService.findAssignments(batch.getId());

        return BatchDetailResponse.builder()
                .id(batch.getId())
                .batchCode(batch.getBatchCode())
                .status(status.name())
                .locked(status.isLocked())
                .greenhouseId(greenhouse == null ? null : greenhouse.getId())
                .greenhouseCode(greenhouse == null ? null : greenhouse.getCode())
                .greenhouseName(greenhouse == null ? null : greenhouse.getName())
                .varietyId(variety == null ? null : variety.getId())
                .varietyName(variety == null ? null : variety.getName())
                .daysPollinationToHarvest(variety == null ? null : variety.getDaysPollinationToHarvest())
                .sowDate(formatDate(batch.getSowDate()))
                .harvestDate(formatDate(batch.getHarvestDate()))
                .pollinationDate(toLocalDateTime(pollinationDate))
                .expectedHarvestDate(expectedHarvestDate(variety, pollinationDate))
                .createdBy(batch.getCreatedBy() == null ? null : batch.getCreatedBy().getId())
                .activatedAt(toLocalDateTime(batch.getActivatedAt()))
                .workerCount((long) assignments.size())
                .eventCount(batchService.countEvents(batch.getId()))
                .assignedUsers(assignments.stream().map(BatchAssignment::getUser).map(this::toWorker).toList())
                .version(batch.getVersion())
                .createdAt(toLocalDateTime(batch.getCreatedAt()))
                .updatedAt(toLocalDateTime(batch.getUpdatedAt()))
                .build();
    }

    private BatchListItemResponse toListItem(Batch batch, Long workerCount, Long pollinationDate) {
        Greenhouse greenhouse = batch.getGreenhouse();
        Variety variety = batch.getVariety();
        BatchStatus status = statusOf(batch);

        return BatchListItemResponse.builder()
                .id(batch.getId())
                .batchCode(batch.getBatchCode())
                .status(status.name())
                .locked(status.isLocked())
                .greenhouseId(greenhouse == null ? null : greenhouse.getId())
                .greenhouseCode(greenhouse == null ? null : greenhouse.getCode())
                .greenhouseName(greenhouse == null ? null : greenhouse.getName())
                .varietyId(variety == null ? null : variety.getId())
                .varietyName(variety == null ? null : variety.getName())
                .sowDate(formatDate(batch.getSowDate()))
                .harvestDate(formatDate(batch.getHarvestDate()))
                .pollinationDate(toLocalDateTime(pollinationDate))
                .expectedHarvestDate(expectedHarvestDate(variety, pollinationDate))
                .createdBy(batch.getCreatedBy() == null ? null : batch.getCreatedBy().getId())
                .activatedAt(toLocalDateTime(batch.getActivatedAt()))
                .workerCount(workerCount)
                .version(batch.getVersion())
                .createdAt(toLocalDateTime(batch.getCreatedAt()))
                .updatedAt(toLocalDateTime(batch.getUpdatedAt()))
                .build();
    }

    private WorkerSummaryResponse toWorker(User user) {
        return WorkerSummaryResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(role -> role.getRoleName().name()).toList())
                .build();
    }

    private String uniqueBatchCode(String greenhouseCode, LocalDate sowDate) {
        String base = "DL-" + (greenhouseCode == null ? "" : greenhouseCode.replace("-", ""))
                + "-" + BATCH_CODE_DATE.format(sowDate);
        String candidate = base;
        int suffix = 1;
        while (batchService.existsByBatchCode(candidate)) {
            suffix++;
            candidate = base + "-" + suffix;
        }
        return candidate;
    }

    private User requireActor(Long actorId) {
        if (actorId == null) {
            throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
        }
        return userRepository.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    private User requireEngineer(Long actorId) {
        User actor = requireActor(actorId);
        if (!hasRole(actor, RoleEnum.ENGINEER)) {
            throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
        }
        return actor;
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

    private static boolean hasRole(User user, RoleEnum role) {
        return user.getRoles().stream().anyMatch(r -> r.getRoleName() == role);
    }

    private static BatchStatus statusOf(Batch batch) {
        return batch.getStatus() == null ? BatchStatus.DRAFT : BatchStatus.valueOf(batch.getStatus());
    }

    private static BatchStatus parseStatus(String status) {
        try {
            return BatchStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ConflictException(ErrorCode.INVALID_STATUS_FILTER);
        }
    }

    private static LocalDateTime toLocalDateTime(Long epochMillis) {
        return epochMillis == null ? null
                : LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
    }

    private static String formatDate(LocalDate date) {
        return date == null ? null : date.toString();
    }

    private static String expectedHarvestDate(Variety variety, Long pollinationDate) {
        if (variety == null || variety.getDaysPollinationToHarvest() == null || pollinationDate == null) {
            return null;
        }
        return LocalDate.ofInstant(Instant.ofEpochMilli(pollinationDate), ZoneId.systemDefault())
                .plusDays(variety.getDaysPollinationToHarvest())
                .toString();
    }

    private static int safePage(int page) {
        return Math.max(page, 0);
    }

    private static int safeSize(int size) {
        return size <= 0 ? 20 : Math.min(size, 100);
    }

    private AuditLog buildAuditLog(Long actorId, String action, String entity, Long entityId, String detail, String ipAddr) {
        return AuditLog.builder()
                .actor(userRepository.findById(actorId).orElse(null))
                .action(action)
                .entity(entity)
                .entityId(entityId)
                .detail(detail)
                .ipAddr(ipAddr)
                .build();
    }

    private String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }
}
