package jakarta.ecofarm_backend.modules.blockchain.service;

import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;

import java.util.List;
import java.util.concurrent.Semaphore;

import jakarta.ecofarm_backend.modules.blockchain.BesuClient;
import jakarta.ecofarm_backend.config.BlockchainConfig;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.AuditLog;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.MerkleSnapshot;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.AnchorRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.AuditLogRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.MerkleSnapshotRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.OutboxEventRepository;
import jakarta.ecofarm_backend.common.dto.AuditError;

@Singleton
public class BlockchainAnchoringService {

    @Inject
    private OutboxEventRepository outboxEventRepository;

    @Inject
    private MerkleSnapshotRepository snapshotRepository;

    @Inject
    private AnchorRepository anchorRepository;

    @Inject
    private AuditLogRepository auditLogRepository;

    @Inject
    private BesuClient besuClient;

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
                        privateAnchor.setChainId(Integer.parseInt(config.getPrivateNetworkId()));
                        privateAnchor.setStatus("SUBMITTED");
                        privateAnchor.setTxHash(txHash);
                        privateAnchor.setAnchoredAt(System.currentTimeMillis());
                        MerkleSnapshot snap = snapshotRepository.findLatest();
                        if (snap != null) {
                            privateAnchor.setSnapshotId(snap.getId());
                        }
                        anchorRepository.persist(privateAnchor);

                        // Audit log
                        AuditLog log = new AuditLog(
                            null, // actorId
                            null, // actorName
                            "OUTBOX_EVENT_ANCHORED",
                            "outboxEvents",
                            event.getId(),
                            privateAnchor.toString(),
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
                new AuditError(errorMsg, event.getBatchIdHash(), event.getSeq()).toString(),
                null
            );
            auditLogRepository.persist(log);
        } catch (Exception ex) { /* ignore audit error during error handling */ }
    }

    // Poll submitted anchors on the private Besu chain; flip to CONFIRMED once confirmed (FR-LDG-003)
    @Schedule(second = "0/5", minute = "*", hour = "*", persistent = false, info = "checkConfirmations")
    public void checkConfirmations() {
        try {
            int required = config.getRequiredConfirmations();
            for (Anchor anchor : anchorRepository.findByStatus("SUBMITTED")) {
                if (anchor.getTxHash() == null) continue;
                try {
                    int confirmations = besuClient.getConfirmations(anchor.getTxHash());
                    anchor.setConfirmations(confirmations);
                    if (anchor.getBlockNumber() == null) {
                        anchor.setBlockNumber(besuClient.getCurrentBlockNumber());
                    }
                    if (confirmations >= required) {
                        anchor.setStatus("CONFIRMED");
                        anchor.setAnchoredAt(System.currentTimeMillis());
                        auditLogRepository.persist(new AuditLog(
                            null, null, "ANCHOR_CONFIRMED", "anchors", anchor.getId(), anchor.toString(), null
                        ));
                    }
                    anchorRepository.persist(anchor);
                } catch (Exception inner) {
                    System.err.println("checkConfirmations: anchor " + anchor.getId() + " failed: " + inner);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in checkConfirmations: " + e.getMessage());
        }
    }
}
