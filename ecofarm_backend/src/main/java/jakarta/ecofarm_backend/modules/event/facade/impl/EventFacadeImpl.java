package jakarta.ecofarm_backend.modules.event.facade.impl;

import jakarta.ecofarm_backend.common.enums.BatchStatus;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.enums.EventChainStatus;
import jakarta.ecofarm_backend.common.enums.EventReviewStatus;
import jakarta.ecofarm_backend.common.enums.EventType;
import jakarta.ecofarm_backend.common.enums.OutboxTarget;
import jakarta.ecofarm_backend.common.exception.ConflictException;
import jakarta.ecofarm_backend.common.exception.EntityNotFoundException;
import jakarta.ecofarm_backend.common.exception.UnprocessableEntityException;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent;
import jakarta.ecofarm_backend.infrastructure.service.BatchService;
import jakarta.ecofarm_backend.infrastructure.service.CultivationEventService;
import jakarta.ecofarm_backend.infrastructure.service.EventHashService;
import jakarta.ecofarm_backend.infrastructure.service.EventSchemaService;
import jakarta.ecofarm_backend.infrastructure.service.OutboxEventService;
import jakarta.ecofarm_backend.infrastructure.service.UserService;
import jakarta.ecofarm_backend.common.dto.request.EventRecordCommand;
import jakarta.ecofarm_backend.modules.event.facade.EventFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class EventFacadeImpl implements EventFacade {

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
        payload.put("schemaVersion", 1);
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
}
