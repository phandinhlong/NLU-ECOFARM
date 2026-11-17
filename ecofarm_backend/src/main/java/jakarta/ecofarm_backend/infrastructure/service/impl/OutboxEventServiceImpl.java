package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.OutboxEventRepository;
import jakarta.ecofarm_backend.infrastructure.service.OutboxEventService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class OutboxEventServiceImpl implements OutboxEventService {

    @Inject
    private OutboxEventRepository outboxEventRepository;

    @Override
    public void persist(OutboxEvent event) {
        this.outboxEventRepository.persist(event);
    }

    @Override
    public OutboxEvent findById(Long id) {
        return this.outboxEventRepository.findById(id);
    }

    @Override
    public List<OutboxEvent> findPending() {
        return this.outboxEventRepository.findPending();
    }

    @Override
    public void incrementAttempts(OutboxEvent event) {
        this.outboxEventRepository.incrementAttempts(event);
    }

    @Override
    public void complete(OutboxEvent event) {
        this.outboxEventRepository.complete(event);
    }

    @Override
    public void markFailed(OutboxEvent event, String errorMsg) {
        this.outboxEventRepository.markFailed(event, errorMsg);
    }

    @Override
    public boolean existsProcessedByBatchIdHashAndSeq(String batchIdHash, int seq) {
        return this.outboxEventRepository.existsProcessedByBatchIdHashAndSeq(batchIdHash, seq);
    }

    @Override
    public int findNextSeqByBatchIdHash(String batchIdHash) {
        return this.outboxEventRepository.findNextSeqByBatchIdHash(batchIdHash);
    }

    @Override
    public OutboxEvent createTestOutbox(String data) {
        OutboxEvent event = OutboxEvent.builder()
                .batchIdHash("0x" + Long.toHexString(System.currentTimeMillis()))
                .seq(1)
                .eventData(data)
                .status(OutboxEvent.OutboxStatus.PENDING)
                .attempts(0)
                .build();
        this.outboxEventRepository.persist(event);
        return event;
    }
}
