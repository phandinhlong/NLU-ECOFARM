package jakarta.ecofarm_backend.modules.event.facade.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ecofarm_backend.common.dto.request.CreateEventRequest;
import jakarta.ecofarm_backend.common.dto.request.EventPageCriteria;
import jakarta.ecofarm_backend.common.dto.request.EventRecordCommand;
import jakarta.ecofarm_backend.common.dto.request.PageCriteria;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.common.dto.response.EventResponse;
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
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Variety;
import jakarta.ecofarm_backend.infrastructure.security.UserSecurityDetailsPrincipal;
import jakarta.ecofarm_backend.infrastructure.service.BatchService;
import jakarta.ecofarm_backend.infrastructure.service.CultivationEventService;
import jakarta.ecofarm_backend.infrastructure.service.EventHashService;
import jakarta.ecofarm_backend.infrastructure.service.EventSchemaService;
import jakarta.ecofarm_backend.infrastructure.service.OutboxEventService;
import jakarta.ecofarm_backend.infrastructure.service.UserService;
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
                .build();
        cultivationEventService.save(event);

        OutboxEvent outbox = OutboxEvent.builder()
                .batchIdHash(batchIdHash)
                .seq(seq)
                .eventData(json)
                .status(OutboxEvent.OutboxStatus.PENDING)
                .attempts(0)
                .eventId(event.getId())
                .targetChain(OutboxTarget.BOTH)
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

    private EventResponse toResponse(CultivationEvent event) {
        Map<String, Object> payload = parsePayload(event.getPayload());
        Map<String, Object> data = payload.get("data") instanceof Map
                ? (Map<String, Object>) payload.get("data")
                : new LinkedHashMap<>();
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
