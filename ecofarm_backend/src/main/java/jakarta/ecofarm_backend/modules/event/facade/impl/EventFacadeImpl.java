package jakarta.ecofarm_backend.modules.event.facade.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ecofarm_backend.common.dto.request.AmendEventRequest;
import jakarta.ecofarm_backend.common.dto.request.CreateEventRequest;
import jakarta.ecofarm_backend.common.dto.request.EventPageCriteria;
import jakarta.ecofarm_backend.common.dto.request.EventRecordCommand;
import jakarta.ecofarm_backend.common.dto.request.PageCriteria;
import jakarta.ecofarm_backend.common.dto.request.ReassignApproverRequest;
import jakarta.ecofarm_backend.common.dto.request.RejectEventRequest;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.common.dto.response.EventResponse;
import jakarta.ecofarm_backend.common.enums.AmendReason;
import jakarta.ecofarm_backend.common.enums.BatchStatus;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.enums.EventChainStatus;
import jakarta.ecofarm_backend.common.enums.EventReviewStatus;
import jakarta.ecofarm_backend.common.enums.EventType;
import jakarta.ecofarm_backend.common.enums.OutboxTarget;
import jakarta.ecofarm_backend.common.enums.RoleEnum;
import jakarta.ecofarm_backend.common.exception.ConflictException;
import jakarta.ecofarm_backend.common.exception.EntityNotFoundException;
import jakarta.ecofarm_backend.common.exception.PermissionDeniedException;
import jakarta.ecofarm_backend.common.exception.UnprocessableEntityException;
import jakarta.ecofarm_backend.common.util.CurrentUserUtil;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.BatchAssignment;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Variety;
import jakarta.ecofarm_backend.infrastructure.security.UserSecurityDetailsPrincipal;
import jakarta.ecofarm_backend.infrastructure.service.BatchService;
import jakarta.ecofarm_backend.infrastructure.service.CultivationEventService;
import jakarta.ecofarm_backend.infrastructure.service.EventHashService;
import jakarta.ecofarm_backend.infrastructure.service.EventSchemaService;
import jakarta.ecofarm_backend.infrastructure.service.OutboxEventService;
import jakarta.ecofarm_backend.infrastructure.service.UserService;
import jakarta.ecofarm_backend.modules.blockchain.client.BesuClient;
import jakarta.ecofarm_backend.modules.event.facade.EventFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class EventFacadeImpl implements EventFacade {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Inject
    private BatchService batchService;

    @Inject
    private CultivationEventService cultivationEventService;

    @Inject
    private OutboxEventService outboxEventService;

    @Inject
    private EventHashService eventHashService;

    @Inject
    private EventSchemaService eventSchemaService;

    @Inject
    private UserService userService;

    @Inject
    private CurrentUserUtil currentUser;

    @Inject
    private BesuClient besuClient;

    @Override
    @Transactional
    public CultivationEvent record(EventRecordCommand command) {
        Long batchId = command.getBatchId();
        EventType type = command.getEventType();
        String clientEventId = command.getClientEventId();
        Long occurredAt = command.getOccurredAt();
        Map<String, Object> data = command.getData();
        Long actorId = command.getActorId();
        Batch batch = batchService.findById(batchId);
        if (batch == null) {
            throw new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND);
        }

        BatchStatus status = batch.getStatus() == null ? BatchStatus.DRAFT : BatchStatus.valueOf(batch.getStatus());
        if (status.isLocked()) {
            throw new ConflictException(ErrorCode.BATCH_LOCKED);
        }

        Optional<CultivationEvent> replay = cultivationEventService.findByClientEventId(clientEventId);
        if (replay.isPresent()) {
            return replay.get();
        }

        long now = System.currentTimeMillis();
        if (occurredAt == null || occurredAt > now) {
            throw new UnprocessableEntityException(ErrorCode.EVENT_ORDER_INVALID);
        }

        List<String> errors = eventSchemaService.validate(type, data);
        if (!errors.isEmpty()) {
            throw new UnprocessableEntityException(ErrorCode.EVENT_SCHEMA_INVALID, errors);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("schemaVersion", command.getSchemaVersion() == null ? 1 : command.getSchemaVersion());
        payload.put("eventType", type.name());
        payload.put("batchCode", batch.getBatchCode());
        payload.put("occurredAt", occurredAt);
        payload.put("actorRef", "U" + actorId);
        payload.put("data", data == null ? new LinkedHashMap<>() : data);

        String json = eventHashService.canonicalJson(payload);
        String hash = eventHashService.hash(payload);
        String batchIdHash = org.web3j.crypto.Hash.sha3String(batch.getBatchCode());
        int seq = outboxEventService.findNextSeqByBatchIdHash(batchIdHash);

        CultivationEvent event = CultivationEvent.builder()
                .batch(batch)
                .seq(seq)
                .eventType(type)
                .payload(json)
                .eventHash(hash)
                .status(EventChainStatus.PENDING)
                .occurredAt(occurredAt)
                .reviewStatus(EventReviewStatus.UNREVIEWED)
                .recordedBy(userService.findById(actorId).orElse(null))
                .recordedAt(now)
                .clientEventId(clientEventId)
                .amendsEvent(command.getAmendsEventId() == null ? null
                        : cultivationEventService.findById(command.getAmendsEventId()).orElse(null))
                .build();
        cultivationEventService.save(event);

        OutboxEvent outbox = OutboxEvent.builder()
                .batchIdHash(batchIdHash)
                .seq(seq)
                .eventData(json)
                .status(OutboxEvent.OutboxStatus.PENDING)
                .attempts(0)
                .eventId(event.getId())
                .targetChain(command.getTargetChain() == null ? OutboxTarget.BOTH : command.getTargetChain())
                .build();
        outboxEventService.persist(outbox);

        return event;
    }

    @Override
    @Transactional
    public EventResponse createEvent(Long batchId, CreateEventRequest request) {
        UserSecurityDetailsPrincipal principal = requirePrincipal();
        Batch batch = batchService.findById(batchId);
        if (batch == null) {
            throw new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND);
        }
        requireAssigned(principal, batchId);

        BatchStatus status = batch.getStatus() == null ? BatchStatus.DRAFT : BatchStatus.valueOf(batch.getStatus());
        if (status.isLocked()) {
            throw new ConflictException(ErrorCode.BATCH_LOCKED);
        }

        Optional<CultivationEvent> replay = cultivationEventService.findByClientEventId(request.getClientEventId());
        if (replay.isPresent()) {
            return toResponse(replay.get());
        }

        long occurredAt;
        try {
            occurredAt = OffsetDateTime.parse(request.getOccurredAt()).toInstant().toEpochMilli();
        } catch (DateTimeParseException e) {
            throw new UnprocessableEntityException(ErrorCode.EVENT_SCHEMA_INVALID, List.of("occurredAt không hợp lệ"));
        }

        long now = System.currentTimeMillis();
        if (occurredAt > now) {
            throw new UnprocessableEntityException(ErrorCode.EVENT_ORDER_INVALID);
        }

        EventType type = request.getEventType();
        List<String> schemaErrors = eventSchemaService.validate(type, request.getData());
        if (!schemaErrors.isEmpty()) {
            throw new UnprocessableEntityException(ErrorCode.EVENT_SCHEMA_INVALID, schemaErrors);
        }

        validateOrdering(type, request.getData(), batch, occurredAt);

        CultivationEvent event = record(EventRecordCommand.builder()
                .batchId(batchId)
                .eventType(type)
                .clientEventId(request.getClientEventId())
                .occurredAt(occurredAt)
                .data(request.getData())
                .actorId(principal.getId())
                .schemaVersion(request.getSchemaVersion())
                .build());

        if (type == EventType.POLLINATION) {
            Variety variety = batch.getVariety();
            if (variety != null && variety.getDaysPollinationToHarvest() != null) {
                LocalDate harvest = Instant.ofEpochMilli(occurredAt).atZone(ZoneId.systemDefault()).toLocalDate()
                        .plusDays(variety.getDaysPollinationToHarvest());
                batch.setHarvestDate(harvest);
                batchService.save(batch);
            }
        }

        return toResponse(event);
    }

    @Override
    public BaseResponse<CursorPageResponse<EventResponse>> findEvents(Long batchId, Long lastIndex, EventReviewStatus reviewStatus) {
        UserSecurityDetailsPrincipal principal = requirePrincipal();
        Batch batch = batchService.findById(batchId);
        if (batch == null) {
            throw new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND);
        }
        requireAssigned(principal, batchId);

        Long cursor = lastIndex == null || lastIndex <= 0 ? null : lastIndex;
        EventPageCriteria criteria = EventPageCriteria.builder()
                .batchId(batchId)
                .lastIndex(cursor)
                .reviewStatus(reviewStatus)
                .limit(PageCriteria.LENGTH + 1)
                .build();
        List<CultivationEvent> rows = cultivationEventService.findPage(criteria);
        boolean hasMore = rows.size() > PageCriteria.LENGTH;
        List<CultivationEvent> page = hasMore ? rows.subList(0, PageCriteria.LENGTH) : rows;
        Long nextIndex = page.isEmpty() ? null : page.get(page.size() - 1).getId();
        List<EventResponse> content = page.stream().map(this::toResponse).toList();
        return BaseResponse.build(new CursorPageResponse<>(content, nextIndex, hasMore));
    }

    @Override
    public BaseResponse<CursorPageResponse<EventResponse>> findReviewQueue(Long lastIndex, Long greenhouseId, EventReviewStatus reviewStatus) {
        UserSecurityDetailsPrincipal principal = requirePrincipal();
        requireEngineer(principal);

        EventReviewStatus filter = reviewStatus == null ? EventReviewStatus.UNREVIEWED : reviewStatus;
        Long cursor = lastIndex == null || lastIndex <= 0 ? null : lastIndex;
        EventPageCriteria criteria = EventPageCriteria.builder()
                .lastIndex(cursor)
                .reviewStatus(filter)
                .limit(PageCriteria.LENGTH + 1)
                .build();
        List<CultivationEvent> rows = cultivationEventService.findReviewPage(greenhouseId, criteria);
        boolean hasMore = rows.size() > PageCriteria.LENGTH;
        List<CultivationEvent> page = hasMore ? rows.subList(0, PageCriteria.LENGTH) : rows;
        Long nextIndex = page.isEmpty() ? null : page.get(page.size() - 1).getId();
        List<EventResponse> content = page.stream().map(this::toResponse).toList();
        return BaseResponse.build(new CursorPageResponse<>(content, nextIndex, hasMore));
    }

    @Override
    public BaseResponse<Long> countReviewQueue(Long greenhouseId, EventReviewStatus reviewStatus) {
        UserSecurityDetailsPrincipal principal = requirePrincipal();
        requireEngineer(principal);

        EventReviewStatus filter = reviewStatus == null ? EventReviewStatus.UNREVIEWED : reviewStatus;
        return BaseResponse.build(cultivationEventService.countReviewPage(greenhouseId, filter));
    }

    @Override
    public BaseResponse<EventResponse> findEvent(Long id) {
        UserSecurityDetailsPrincipal principal = requirePrincipal();
        CultivationEvent event = loadEvent(id);
        requireAssigned(principal, event.getBatch().getId());
        return BaseResponse.build(toResponse(event));
    }

    @Override
    @Transactional
    public BaseResponse<EventResponse> approve(Long id) {
        UserSecurityDetailsPrincipal principal = requirePrincipal();
        requireEngineer(principal);
        CultivationEvent event = loadEvent(id);
        if (event.getReviewStatus() != EventReviewStatus.UNREVIEWED) {
            throw new ConflictException(ErrorCode.EVENT_ALREADY_REVIEWED);
        }
        if (event.getStatus() == EventChainStatus.SUPERSEDED) {
            throw new ConflictException(ErrorCode.EVENT_ALREADY_SUPERSEDED);
        }
        Batch batch = event.getBatch();
        ensureNotLocked(batch);
        approvalScope(event, principal, true);

        long now = System.currentTimeMillis();
        event.setReviewStatus(EventReviewStatus.APPROVED);
        event.setReviewedBy(userService.findById(principal.getId()).orElse(null));
        event.setReviewedAt(now);
        cultivationEventService.save(event);

        createMetaEvent(batch, EventType.EVENT_APPROVED, event.getEventHash(), principal.getId(), now, OutboxTarget.BOTH, null);
        return BaseResponse.build(toResponse(event));
    }

    @Override
    @Transactional
    public BaseResponse<EventResponse> reject(Long id, RejectEventRequest request) {
        UserSecurityDetailsPrincipal principal = requirePrincipal();
        requireEngineer(principal);
        CultivationEvent event = loadEvent(id);
        if (event.getReviewStatus() != EventReviewStatus.UNREVIEWED) {
            throw new ConflictException(ErrorCode.EVENT_ALREADY_REVIEWED);
        }
        if (event.getStatus() == EventChainStatus.SUPERSEDED) {
            throw new ConflictException(ErrorCode.EVENT_ALREADY_SUPERSEDED);
        }
        Batch batch = event.getBatch();
        ensureNotLocked(batch);
        approvalScope(event, principal, true);

        long now = System.currentTimeMillis();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("targetEventId", event.getId());
        data.put("targetEventHash", event.getEventHash());
        data.put("reason", request.getReason());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("schemaVersion", 1);
        payload.put("eventType", EventType.EVENT_REJECTED.name());
        payload.put("batchCode", batch.getBatchCode());
        payload.put("occurredAt", now);
        payload.put("actorRef", "U" + principal.getId());
        payload.put("data", data);
        String json = eventHashService.canonicalJson(payload);
        String hash = eventHashService.hash(payload);
        String batchIdHash = org.web3j.crypto.Hash.sha3String(batch.getBatchCode());
        int seq = outboxEventService.findNextSeqByBatchIdHash(batchIdHash);

        try {
            besuClient.sendTransaction(json);
        } catch (Exception e) {
            throw new ConflictException(ErrorCode.ANCHOR_FAILED, List.of("Không thể ghi từ chối lên chuỗi riêng: " + e.getMessage()));
        }

        event.setReviewStatus(EventReviewStatus.REJECTED);
        event.setReviewedBy(userService.findById(principal.getId()).orElse(null));
        event.setReviewedAt(now);
        event.setRejectReason(request.getReason());
        cultivationEventService.save(event);

        CultivationEvent meta = CultivationEvent.builder()
                .batch(batch)
                .seq(seq)
                .eventType(EventType.EVENT_REJECTED)
                .payload(json)
                .eventHash(hash)
                .status(EventChainStatus.CONFIRMED)
                .occurredAt(now)
                .reviewStatus(EventReviewStatus.APPROVED)
                .recordedBy(event.getRecordedBy())
                .recordedAt(now)
                .clientEventId(UUID.randomUUID().toString())
                .build();
        cultivationEventService.save(meta);

        OutboxEvent outbox = OutboxEvent.builder()
                .batchIdHash(batchIdHash)
                .seq(seq)
                .eventData(json)
                .status(OutboxEvent.OutboxStatus.SUCCESS)
                .attempts(0)
                .eventId(meta.getId())
                .targetChain(OutboxTarget.PRIVATE)
                .build();
        outboxEventService.persist(outbox);

        return BaseResponse.build(toResponse(event));
    }

    @Override
    @Transactional
    public BaseResponse<EventResponse> reassignApprover(Long id, ReassignApproverRequest request) {
        UserSecurityDetailsPrincipal principal = requirePrincipal();
        requireAdmin(principal);
        CultivationEvent event = loadEvent(id);
        if (event.getReviewStatus() != EventReviewStatus.UNREVIEWED) {
            throw new ConflictException(ErrorCode.EVENT_ALREADY_REVIEWED);
        }
        User engineer = userService.findById(request.getEngineerId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
        if (engineer.getRoles() == null || engineer.getRoles().stream().noneMatch(r -> r.getRoleName() == RoleEnum.ENGINEER)) {
            throw new ConflictException(ErrorCode.WORKER_INVALID_ROLE);
        }
        event.setPendingApprover(engineer);
        cultivationEventService.save(event);
        return BaseResponse.build(toResponse(event));
    }

    @Override
    @Transactional
    public BaseResponse<EventResponse> amend(Long id, AmendEventRequest request) {
        UserSecurityDetailsPrincipal principal = requirePrincipal();
        requireEngineer(principal);
        CultivationEvent target = loadEvent(id);
        if (target.getStatus() == EventChainStatus.SUPERSEDED) {
            throw new ConflictException(ErrorCode.EVENT_ALREADY_SUPERSEDED);
        }
        Batch batch = target.getBatch();
        ensureNotLocked(batch);
        approvalScope(target, principal, true);

        if (request.getReasonCode() == AmendReason.OTHER && (request.getNote() == null || request.getNote().isBlank())) {
            throw new UnprocessableEntityException(ErrorCode.EVENT_SCHEMA_INVALID, List.of("Ghi chú là bắt buộc khi lý do là Khác"));
        }

        long occurredAt;
        try {
            occurredAt = OffsetDateTime.parse(request.getOccurredAt()).toInstant().toEpochMilli();
        } catch (DateTimeParseException e) {
            throw new UnprocessableEntityException(ErrorCode.EVENT_SCHEMA_INVALID, List.of("occurredAt không hợp lệ"));
        }
        long now = System.currentTimeMillis();
        if (occurredAt > now) {
            throw new UnprocessableEntityException(ErrorCode.EVENT_ORDER_INVALID);
        }

        List<String> errors = eventSchemaService.validate(target.getEventType(), request.getData());
        if (!errors.isEmpty()) {
            throw new UnprocessableEntityException(ErrorCode.EVENT_SCHEMA_INVALID, errors);
        }

        validateOrdering(target.getEventType(), request.getData(), batch, occurredAt);

        CultivationEvent newEvent = record(EventRecordCommand.builder()
                .batchId(batch.getId())
                .eventType(target.getEventType())
                .clientEventId(request.getClientEventId())
                .occurredAt(occurredAt)
                .data(request.getData())
                .actorId(principal.getId())
                .schemaVersion(1)
                .targetChain(OutboxTarget.BOTH)
                .amendsEventId(target.getId())
                .build());
        newEvent.setAmendReason(request.getReasonCode());
        newEvent.setAmendNote(request.getNote());
        newEvent.setReviewStatus(EventReviewStatus.UNREVIEWED);
        cultivationEventService.save(newEvent);

        target.setStatus(EventChainStatus.SUPERSEDED);
        cultivationEventService.save(target);

        return BaseResponse.build(toResponse(newEvent));
    }

    private CultivationEvent loadEvent(Long id) {
        return cultivationEventService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.EVENT_NOT_FOUND));
    }

    private void ensureNotLocked(Batch batch) {
        BatchStatus status = batch.getStatus() == null ? BatchStatus.DRAFT : BatchStatus.valueOf(batch.getStatus());
        if (status.isLocked()) {
            throw new ConflictException(ErrorCode.BATCH_LOCKED);
        }
    }

    private void approvalScope(CultivationEvent event, UserSecurityDetailsPrincipal principal, boolean allowAdminOverride) {
        User recorder = event.getRecordedBy();
        BatchAssignment assignment = findRecorderAssignment(event);
        User assigner = assignment == null ? null : assignment.getAssignedBy();
        boolean assignerActive = assigner != null && assigner.isActive();
        boolean isAdmin = principal.hasRole(RoleEnum.ADMIN.name());

        if (recorder != null && recorder.getId().equals(principal.getId())) {
            throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
        }
        if (event.getPendingApprover() != null && event.getPendingApprover().getId().equals(principal.getId())) {
            return;
        }
        if (assignerActive) {
            if (!assigner.getId().equals(principal.getId()) && !(allowAdminOverride && isAdmin)) {
                throw new PermissionDeniedException(ErrorCode.EVENT_APPROVER_NOT_ASSIGNED);
            }
        } else {
            if (!isAdmin) {
                throw new PermissionDeniedException(ErrorCode.EVENT_APPROVER_NOT_ASSIGNED);
            }
        }
    }

    private void requireEngineer(UserSecurityDetailsPrincipal principal) {
        if (!principal.hasRole(RoleEnum.ENGINEER.name())) {
            throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
        }
    }

    private void requireAdmin(UserSecurityDetailsPrincipal principal) {
        if (!principal.hasRole(RoleEnum.ADMIN.name())) {
            throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
        }
    }

    private void createMetaEvent(Batch batch, EventType type, String targetHash, Long actorId, long now, OutboxTarget target, Long amendsId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("targetEventHash", targetHash);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("schemaVersion", 1);
        payload.put("eventType", type.name());
        payload.put("batchCode", batch.getBatchCode());
        payload.put("occurredAt", now);
        payload.put("actorRef", "U" + actorId);
        payload.put("data", data);
        String json = eventHashService.canonicalJson(payload);
        String hash = eventHashService.hash(payload);
        String batchIdHash = org.web3j.crypto.Hash.sha3String(batch.getBatchCode());
        int seq = outboxEventService.findNextSeqByBatchIdHash(batchIdHash);

        CultivationEvent meta = CultivationEvent.builder()
                .batch(batch)
                .seq(seq)
                .eventType(type)
                .payload(json)
                .eventHash(hash)
                .status(EventChainStatus.PENDING)
                .occurredAt(now)
                .reviewStatus(EventReviewStatus.APPROVED)
                .recordedBy(userService.findById(actorId).orElse(null))
                .recordedAt(now)
                .clientEventId(UUID.randomUUID().toString())
                .build();
        cultivationEventService.save(meta);

        OutboxEvent outbox = OutboxEvent.builder()
                .batchIdHash(batchIdHash)
                .seq(seq)
                .eventData(json)
                .status(OutboxEvent.OutboxStatus.PENDING)
                .attempts(0)
                .eventId(meta.getId())
                .targetChain(target)
                .build();
        outboxEventService.persist(outbox);
    }

    private BatchAssignment findRecorderAssignment(CultivationEvent event) {
        if (event.getBatch() == null || event.getRecordedBy() == null) {
            return null;
        }
        return batchService.findAssignments(event.getBatch().getId()).stream()
                .filter(a -> a.getUser() != null && a.getUser().getId().equals(event.getRecordedBy().getId()))
                .findFirst().orElse(null);
    }

    private EventResponse toResponse(CultivationEvent event) {
        Map<String, Object> payload = parsePayload(event.getPayload());
        Map<String, Object> data = payload.get("data") instanceof Map
                ? (Map<String, Object>) payload.get("data")
                : new LinkedHashMap<>();
        BatchAssignment assignment = findRecorderAssignment(event);
        User assigner = assignment == null ? null : assignment.getAssignedBy();
        return EventResponse.builder()
                .id(event.getId())
                .batchId(event.getBatch() == null ? null : event.getBatch().getId())
                .seq(event.getSeq())
                .eventType(event.getEventType() == null ? null : event.getEventType().name())
                .status(event.getStatus() == null ? null : event.getStatus().name())
                .reviewStatus(event.getReviewStatus() == null ? null : event.getReviewStatus().name())
                .data(data)
                .eventHash(event.getEventHash())
                .recordedById(event.getRecordedBy() == null ? null : event.getRecordedBy().getId())
                .recordedByName(event.getRecordedBy() == null ? null : event.getRecordedBy().getFullName())
                .occurredAt(toLocalDateTime(event.getOccurredAt()))
                .recordedAt(toLocalDateTime(event.getRecordedAt()))
                .clientEventId(event.getClientEventId())
                .amendsEventId(event.getAmendsEvent() == null ? null : event.getAmendsEvent().getId())
                .batchCode(event.getBatch() == null ? null : event.getBatch().getBatchCode())
                .greenhouseId(event.getBatch() == null || event.getBatch().getGreenhouse() == null ? null : event.getBatch().getGreenhouse().getId())
                .greenhouseName(event.getBatch() == null || event.getBatch().getGreenhouse() == null ? null : event.getBatch().getGreenhouse().getName())
                .pendingApproverId(event.getPendingApprover() == null ? null : event.getPendingApprover().getId())
                .assignerId(assigner == null ? null : assigner.getId())
                .assignerActive(assigner == null ? null : assigner.isActive())
                .rejectReason(event.getRejectReason())
                .amendReason(event.getAmendReason() == null ? null : event.getAmendReason().name())
                .amendNote(event.getAmendNote())
                .build();
    }

    private static LocalDateTime toLocalDateTime(Long epochMillis) {
        return epochMillis == null ? null
                : LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parsePayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return MAPPER.readValue(payload, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private void validateOrdering(EventType type, Map<String, Object> data, Batch batch, long occurredAt) {
        if (type == EventType.POLLINATION && batch.getSowDate() != null) {
            long sowMillis = batch.getSowDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            if (occurredAt < sowMillis) {
                throw new UnprocessableEntityException(ErrorCode.EVENT_ORDER_INVALID, List.of("Thụ phấn phải sau khi xuống giống"));
            }
        }
        if (type == EventType.POLLINATION_ISOLATION) {
            Variety variety = batch.getVariety();
            if (variety != null && variety.getMinIsolationDays() != null && data != null) {
                Object raw = data.get("isolationDays");
                if (raw instanceof Number number && number.intValue() < variety.getMinIsolationDays()) {
                    throw new UnprocessableEntityException(ErrorCode.EVENT_ORDER_INVALID,
                            List.of("Số ngày cách li nhỏ hơn tối thiểu " + variety.getMinIsolationDays()));
                }
            }
        }
    }

    private UserSecurityDetailsPrincipal requirePrincipal() {
        UserSecurityDetailsPrincipal principal = currentUser.get();
        if (principal == null || principal.getId() == null) {
            throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
        }
        return principal;
    }

    private void requireAssigned(UserSecurityDetailsPrincipal principal, Long batchId) {
        if (principal.hasRole(RoleEnum.FARMER.name()) && !principal.hasRole(RoleEnum.ENGINEER.name())
                && !Boolean.TRUE.equals(batchService.isAssigned(batchId, principal.getId()))) {
            throw new PermissionDeniedException(ErrorCode.BATCH_NOT_ASSIGNED);
        }
    }
}
