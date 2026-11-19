package jakarta.ecofarm_backend.modules.batch.facade.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ecofarm_backend.common.dto.request.AuditLogParam;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.request.PageCriteria;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
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
import jakarta.ecofarm_backend.infrastructure.persistence.repository.VarietyRepository;
import jakarta.ecofarm_backend.infrastructure.service.UserService;
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
import jakarta.transaction.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private UserService userService;

    @Inject
    private AuditLogRepository auditLogRepository;

    @Inject
    private CurrentUserUtil currentUser;

    @Inject
    private HttpServletRequest httpRequest;

    @Override
    public BaseResponse<CursorPageResponse<BatchListItemResponse>> findBatchesByCriteria(PageCriteria criteria) {
        UserSecurityDetailsPrincipal principal = requirePrincipal();
        criteria.setMineUserId(Boolean.TRUE.equals(criteria.getMine()) && hasRole(principal, RoleEnum.FARMER) ? principal.getId() : null);

        List<Batch> batches = batchService.findPage(criteria);
        boolean hasMore = batches.size() > PageCriteria.LENGTH;
        List<Batch> page = hasMore ? batches.subList(0, PageCriteria.LENGTH) : batches;

        List<Long> ids = page.stream().map(Batch::getId).toList();
        Map<Long, Long> workerCounts = batchService.countAssignmentsByBatchIds(ids);
        Map<Long, Long> pollinationDates = batchService.findPollinationDates(ids);

        List<BatchListItemResponse> content = page.stream()
                .map(b -> toListItem(b, workerCounts.getOrDefault(b.getId(), 0L), pollinationDates.get(b.getId())))
                .toList();

        Long lastIndex = page.isEmpty() ? null : page.get(page.size() - 1).getId();
        return BaseResponse.build(new CursorPageResponse<>(content, lastIndex, hasMore));
    }

    @Override
    public BaseResponse<BatchDetailResponse> findBatchDetail(Long batchId) {
        UserSecurityDetailsPrincipal principal = requirePrincipal();
        Batch batch = batchService.findById(batchId);
        if (batch == null) {
            throw new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND);
        }

        if (hasRole(principal, RoleEnum.FARMER) && !batchService.isAssigned(batchId, principal.getId())) {
            throw new PermissionDeniedException(ErrorCode.BATCH_NOT_ASSIGNED);
        }

        return BaseResponse.build(toDetail(batch));
    }

    @Override
    @Transactional
    public BaseResponse<BatchDetailResponse> createBatch(CreateBatchRequest request) {
        Long actorId = resolveActorId();
        String ipAddr = resolveClientIp();
        User actor = requireEngineerOrAdmin(actorId);

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

        auditLogRepository.persist(buildAuditLog(AuditLogParam.builder()
                .actorId(actorId)
                .action("BATCH_CREATE")
                .entity("batches")
                .entityId(batch.getId())
                .detail(toJson(Map.of("batchCode", batch.getBatchCode(), "status", batch.getStatus())))
                .ipAddr(ipAddr)
                .build()));

        return BaseResponse.build(toDetail(batch));
    }

    @Override
    public BaseResponse<CursorPageResponse<WorkerSummaryResponse>> findBatchWorkers(Long lastIndex) {
        Long cursor = lastIndex == null || lastIndex <= 0 ? null : lastIndex;
        List<User> users = userService.findActiveWorkerPage(cursor, PageCriteria.LENGTH + 1);
        boolean hasMore = users.size() > PageCriteria.LENGTH;
        List<User> page = hasMore ? users.subList(0, PageCriteria.LENGTH) : users;
        Long nextIndex = page.isEmpty() ? null : page.get(page.size() - 1).getId();
        List<WorkerSummaryResponse> content = page.stream().map(this::toWorker).toList();
        return BaseResponse.build(new CursorPageResponse<>(content, nextIndex, hasMore));
    }

    @Override
    public BaseResponse<CursorPageResponse<WorkerSummaryResponse>> findAssignedWorkersByBatchId(Long batchId, Long lastIndex) {
        Batch batch = batchService.findById(batchId);
        if (batch == null) {
            throw new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND);
        }
        Long cursor = lastIndex == null || lastIndex <= 0 ? null : lastIndex;
        List<User> users = batchService.findAssignedWorkerPage(batchId, cursor, PageCriteria.LENGTH + 1);
        boolean hasMore = users.size() > PageCriteria.LENGTH;
        List<User> page = hasMore ? users.subList(0, PageCriteria.LENGTH) : users;
        Long nextIndex = page.isEmpty() ? null : page.get(page.size() - 1).getId();
        List<WorkerSummaryResponse> content = page.stream().map(this::toWorker).toList();
        return BaseResponse.build(new CursorPageResponse<>(content, nextIndex, hasMore));
    }

    @Override
    @Transactional
    public BaseResponse<Boolean> addWorkers(Long batchId, AssignWorkersRequest request) {
        Long actorId = resolveActorId();
        String ipAddr = resolveClientIp();
        requireEngineerOrAdmin(actorId);

        Batch batch = batchService.findById(batchId);
        if (batch == null) {
            throw new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND);
        }
        if (statusOf(batch).isLocked()) {
            throw new ConflictException(ErrorCode.BATCH_LOCKED);
        }

        List<Long> userIds = request.getUserIds() == null ? List.of() : request.getUserIds().stream().distinct().toList();
        Set<Long> existing = new HashSet<>(batchService.findAssignedUserIds(batchId));
        List<String> errors = new ArrayList<>();
        List<User> toAdd = new ArrayList<>();
        for (Long userId : userIds) {
            if (existing.contains(userId)) {
                continue;
            }
            User worker = userService.findById(userId).orElse(null);
            if (worker == null || !worker.isActive() || !hasRole(worker, RoleEnum.FARMER)) {
                errors.add("Người lao động " + userId + " không hợp lệ");
                continue;
            }
            toAdd.add(worker);
        }
        boolean hasErrors = errors.size() > 0;
        if (hasErrors) {
            throw new ConflictException(ErrorCode.INVALID_WORKER, errors);
        }

        List<Long> added = new ArrayList<>();
        for (User worker : toAdd) {
            batchService.persistAssignment(BatchAssignment.builder().batch(batch).assignee(worker).build());
            added.add(worker.getId());
        }

        auditLogRepository.persist(buildAuditLog(AuditLogParam.builder()
                .actorId(actorId)
                .action("BATCH_ADD_WORKERS")
                .entity("batches")
                .entityId(batchId)
                .detail(toJson(Map.of("userIds", added)))
                .ipAddr(ipAddr)
                .build()));

        return BaseResponse.build(Boolean.TRUE);
    }

    @Override
    @Transactional
    public BaseResponse<Boolean> removeWorkers(Long batchId, AssignWorkersRequest request) {
        Long actorId = resolveActorId();
        String ipAddr = resolveClientIp();
        requireEngineerOrAdmin(actorId);

        Batch batch = batchService.findById(batchId);
        if (batch == null) {
            throw new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND);
        }
        if (statusOf(batch).isLocked()) {
            throw new ConflictException(ErrorCode.BATCH_LOCKED);
        }

        List<Long> userIds = request.getUserIds() == null ? List.of() : request.getUserIds().stream().distinct().toList();
        batchService.removeAssignments(batchId, userIds);

        auditLogRepository.persist(buildAuditLog(AuditLogParam.builder()
                .actorId(actorId)
                .action("BATCH_REMOVE_WORKERS")
                .entity("batches")
                .entityId(batchId)
                .detail(toJson(Map.of("userIds", userIds)))
                .ipAddr(ipAddr)
                .build()));

        return BaseResponse.build(Boolean.TRUE);
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
                .assignedUsers(assignments.stream().map(BatchAssignment::getAssignee).map(this::toWorker).toList())
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
        return userService.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    private User requireEngineerOrAdmin(Long actorId) {
        User actor = requireActor(actorId);
        if (!hasRole(actor, RoleEnum.ENGINEER) && !hasRole(actor, RoleEnum.ADMIN)) {
            throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
        }
        return actor;
    }

    private UserSecurityDetailsPrincipal requirePrincipal() {
        UserSecurityDetailsPrincipal principal = currentUser.get();
        if (principal == null || principal.getId() == null) {
            throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
        }
        return principal;
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

    private static boolean hasRole(UserSecurityDetailsPrincipal principal, RoleEnum role) {
        return principal.hasRole(role.name());
    }

    private static boolean hasRole(User user, RoleEnum role) {
        return user.getRoles().stream().anyMatch(r -> r.getRoleName() == role);
    }

    private static BatchStatus statusOf(Batch batch) {
        return batch.getStatus() == null ? BatchStatus.DRAFT : BatchStatus.valueOf(batch.getStatus());
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

    private AuditLog buildAuditLog(AuditLogParam param) {
        if (param == null) return null;
        return AuditLog.builder()
                .actor(param.getActorId() != null ? userService.findById(param.getActorId()).orElse(null) : null)
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
}
