package jakarta.ecofarm_backend.service;

import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.List;

import jakarta.ecofarm_backend.persistence.entities.OutboxEvent;
import jakarta.ecofarm_backend.persistence.repository.OutboxEventRepository;
import jakarta.ecofarm_backend.blockchain.BesuClient;

@Stateless
public class BlockchainAnchoringService {

    @Inject
    private OutboxEventRepository outboxEventRepository;

    @Inject
    private BesuClient besuClient;

    @Schedule(second = "*/5", minute = "*", hour = "*", persistent = false)
    public void processOutbox() {
        try {
            List<OutboxEvent> pending = outboxEventRepository.findPending();
            for (OutboxEvent event : pending) {
                // Idempotency check: if already processed by another thread, skip
                if (outboxEventRepository.existsProcessedByBatchIdHashAndSeq(event.getBatchIdHash(), event.getSeq())) {
                    outboxEventRepository.complete(event);
                    continue;
                }

                try {
                    String txHash = besuClient.sendTransaction(event.getEventData());
                    if (txHash != null && !txHash.isEmpty()) {
                        // In real implementation, create anchor, verify confirmations, etc.
                        outboxEventRepository.complete(event);
                    } else {
                        outboxEventRepository.markFailed(event, "Invalid response");
                    }
                } catch (Exception e) {
                    outboxEventRepository.incrementAttempts(event);
                }
            }
        } catch (Exception e) {
            // Log exception
        }
    }

    public void enqueueEvent(OutboxEvent event) {
        outboxEventRepository.persist(event);
    }
}
