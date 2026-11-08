package jakarta.ecofarm_backend.service;

import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Semaphore;

import jakarta.ecofarm_backend.blockchain.BesuClient;
import jakarta.ecofarm_backend.blockchain.PolygonClient;
import jakarta.ecofarm_backend.config.BlockchainConfig;
import jakarta.ecofarm_backend.persistence.entities.Anchor;
import jakarta.ecofarm_backend.persistence.entities.AuditLog;
import jakarta.ecofarm_backend.persistence.entities.OutboxEvent;
import jakarta.ecofarm_backend.persistence.repository.AnchorRepository;
import jakarta.ecofarm_backend.persistence.repository.AuditLogRepository;
import jakarta.ecofarm_backend.persistence.repository.OutboxEventRepository;
import jakarta.ecofarm_backend.dto.AuditError;

@Stateless
public class BlockchainAnchoringService {

    @Inject
    private OutboxEventRepository outboxEventRepository;

    @Inject
    private AnchorRepository anchorRepository;

    @Inject
    private AuditLogRepository auditLogRepository;

    @Inject
    private BesuClient besuClient;

    @Inject
    private PolygonClient polygonClient;

    @Inject
    private BlockchainConfig config;

    // Rate limiter: max 2 blockchain transactions per second (as per FR-LDG-006)
    private final Semaphore rateLimiter = new Semaphore(2);

    @Schedule(second = "*/1", minute = "*", hour = "*", persistent = false)
    public void processOutbox() {
        try {
            List<OutboxEvent> pending = outboxEventRepository.findPending();
            int processed = 0;
            for (OutboxEvent event : pending) {
                if (processed >= 2) break; // respect rate limit
                try {
                    // Rate limiting
                    rateLimiter.acquire();
                    
                    // Idempotency check: if already processed by another thread, skip
                    if (outboxEventRepository.existsProcessedByBatchIdHashAndSeq(event.getBatchIdHash(), event.getSeq())) {
                        outboxEventRepository.complete(event);
                        processed++;
                        continue;
                    }

                    // Send to private chain (Besu)
                    String txHash = besuClient.sendTransaction(event.getEventData());
                    if (txHash != null && !txHash.isEmpty()) {
                        // Create anchor record for private chain
                        Anchor privateAnchor = new Anchor();
                        // Add anchor details (snapshotId, chainId, contractAddress)
                        privateAnchor.setStatus("SUCCESS");
                        privateAnchor.setTxHash(txHash);
                        privateAnchor.setAnchoredAt(Instant.now());
                        anchorRepository.persist(privateAnchor);

                        // Audit log
                        AuditLog log = new AuditLog(
                            null, // actorId
                            null, // actorName
                            "OUTBOX_EVENT_ANCHORED",
                            "outboxEvents",
                            event.getId(),
                            privateAnchor,
                            null
                        );
                        auditLogRepository.persist(log);

                        // Mark outbox as processed
                        outboxEventRepository.complete(event);
                        processed++;
                    } else {
                        outboxEventRepository.markFailed(event, "Invalid response from private chain");
                    }
                } catch (Exception e) {
                    outboxEventRepository.incrementAttempts(event);
                    // Audit error
                    auditLogError(event, e.getMessage());
                } finally {
                    rateLimiter.release();
                }
            }
        } catch (Exception e) {
            // Log error
            System.err.println("Error in processOutbox: " + e.getMessage());
        }
    }

    private void auditLogError(OutboxEvent event, String errorMsg) {
        try {
            AuditLog log = new AuditLog(
                null,
                null,
                "OUTBOX_PROCESS_ERROR",
                "outboxEvents",
                event.getId(),
                new AuditError(errorMsg, event.getBatchIdHash(), event.getSeq()),
                null
            );
            auditLogRepository.persist(log);
        } catch (Exception ex) { /* ignore audit error during error handling */ }
    }

    // Simulate consumption of confirmed FP from public chain (FR-LDG-003)
    public void checkPublicConfirmation(Anchor anchor) throws Exception {
        if (!"SUCCESS".equals(anchor.getStatus())) return;
        int confirmations = polygonClient.getConfirmations(anchor.getTxHash());
        int required = config.getRequiredConfirmations();
        
        if (confirmations >= required) {
            anchor.setStatus("CONFIRMED_PUBLIC"); // or ACTIVE
            anchor.setAnchoredAt(Instant.now());
            anchorRepository.persist(anchor);
            
            // Audit log
            auditLogRepository.persist(new AuditLog(
                null, null, "PUBLIC_CONFIRMED", "anchors", anchor.getId(), anchor, null
            ));
        }
    }

}
