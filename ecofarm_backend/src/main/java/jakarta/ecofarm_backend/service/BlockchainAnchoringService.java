package jakarta.ecofarm_backend.service;

import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
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
                    String PrivateTxHash = besuClient.sendTransaction(event.getEventData());
                    if (PrivateTxHash != null && !PrivateTxHash.isEmpty()) {
                        // Create anchor record for private chain
                        Anchor privateAnchor = new Anchor();
                        // Add anchor details (snapshotId, chainId, contractAddress)
                        privateAnchor.setChainId(Integer.valueOf(config.getPrivateNetworkId()));
                        privateAnchor.setStatus("SUCCESS");
                        privateAnchor.setTxHash(PrivateTxHash);
privateAnchor.setAnchoredAt(System.currentTimeMillis() / 1000);
                        anchorRepository.persist(privateAnchor);

                        // Audit log
                        AuditLog privateLog = new AuditLog(
                            null, // actorId
                            null, // actorName
                            "OUTBOX_EVENT_ANCHORED",
                            "outboxEvents",
                            event.getId(),
                            privateAnchor.toString(),
                            null
                        );
                        auditLogRepository.persist(privateLog);
                        String publicTxHash = polygonClient.sendTransaction(event.getEventData());
                        if(publicTxHash != null && !publicTxHash.isEmpty()){
                            Anchor publicAnchor = new Anchor();
                            publicAnchor.setChainId(config.getPublicChainId());
                            publicAnchor.setStatus("SUCCESS");
                            publicAnchor.setTxHash(publicTxHash);
                            publicAnchor.setExplorerUrl("https://amoy.polygonscan.com/tx/"+publicTxHash);
                            publicAnchor.setAnchoredAt(System.currentTimeMillis() / 1000);
                            anchorRepository.persist(publicAnchor);

                            // Audit log public chain
                            AuditLog publicLog = new AuditLog(
                                    null,
                                    null,
                                    "OUTBOX_EVENT_ANCHORED_PUBLIC",
                                    "outboxEvents",
                                    event.getId(),
                                    publicAnchor.toString(),
                                    null
                            );
                            auditLogRepository.persist(publicLog);
                        }

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
    @Schedule(second = "*/15", minute = "*", hour = "*", persistent = false)
    public void verifyPublicAnchors() {
        try {
            List<Anchor> anchors = anchorRepository.findByStatus("SUCCESS");
            for (Anchor anchor : anchors) {
                if (config.getPublicChainId().equals(anchor.getChainId())) {
                    try {
                        checkPublicConfirmation(anchor);
                    } catch (Exception e) {
                        System.err.println("Error checking confirmation for tx " + anchor.getTxHash() + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error in verifyPublicAnchors: " + e.getMessage());
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

    // Simulate consumption of confirmed FP from public chain (FR-LDG-003)
    public void checkPublicConfirmation(Anchor anchor) throws Exception {
        if (!"SUCCESS".equals(anchor.getStatus())) return;
        if (!config.getPublicChainId().equals(anchor.getChainId())) return;
        int confirmations = polygonClient.getConfirmations(anchor.getTxHash());
        int required = config.getRequiredConfirmations();
        anchor.setConfirmations(confirmations);
        if (confirmations >= required) {
            anchor.setStatus("CONFIRMED_PUBLIC"); // or ACTIVE
anchor.setAnchoredAt(System.currentTimeMillis() / 1000);
            anchorRepository.persist(anchor);

            // Audit log
            auditLogRepository.persist(new AuditLog(
                null, null, "PUBLIC_CONFIRMED", "anchors", anchor.getId(), anchor.toString(), null
            ));
        }
    }

}
