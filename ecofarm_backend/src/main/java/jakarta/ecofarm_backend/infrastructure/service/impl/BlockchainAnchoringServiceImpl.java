package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;

import java.util.List;
import java.util.concurrent.Semaphore;

import jakarta.ecofarm_backend.common.dto.response.AuditError;
import jakarta.ecofarm_backend.config.BlockchainConfig;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.AuditLog;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent;
import jakarta.ecofarm_backend.infrastructure.service.AnchorService;
import jakarta.ecofarm_backend.infrastructure.service.AuditLogService;
import jakarta.ecofarm_backend.infrastructure.service.BlockchainAnchoringService;
import jakarta.ecofarm_backend.infrastructure.service.OutboxEventService;
import jakarta.ecofarm_backend.modules.blockchain.client.BesuClient;
import jakarta.ecofarm_backend.modules.blockchain.client.PolygonClient;

@Singleton
public class BlockchainAnchoringServiceImpl implements BlockchainAnchoringService {

    @Inject
    private OutboxEventService outboxEventService;

    @Inject
    private AnchorService anchorService;

    @Inject
    private AuditLogService auditLogService;

    @Inject
    private BesuClient besuClient;

    @Inject
    private BlockchainConfig config;

    @Inject
    private PolygonClient polygonClient;

    private final Semaphore rateLimiter = new Semaphore(2);

    @Override
    @Schedule(second = "*/10", minute = "*", hour = "*", persistent = false)
    public void processOutbox() {
        try {
            List<OutboxEvent> pending = outboxEventService.findPending();
            int processed = 0;
            for (OutboxEvent event : pending) {
                if (processed >= 2) break;
                try {
                    rateLimiter.acquire();
                    try {
                        processSingleEvent(event);
                        processed++;
                    } finally {
                        rateLimiter.release();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error in processOutbox: " + e.getMessage());
        }
    }

    private void processSingleEvent(OutboxEvent event) {
        try {
            if (outboxEventService.existsProcessedByBatchIdHashAndSeq(event.getBatchIdHash(), event.getSeq())) {
                outboxEventService.complete(event);
                return;
            }

            // Gửi lên Besu Private Chain
            String txHash = besuClient.sendTransaction(event.getEventData());

            Anchor anchor = new Anchor();
            anchor.setTxHash(txHash);
            anchor.setStatus("SUBMITTED");
            anchor.setBlockNumber(besuClient.getCurrentBlockNumber());
            anchor.setConfirmations(0);
            anchor.setExplorerUrl("http://localhost:8545/tx/" + txHash);
            anchorService.persist(anchor);

            // Gửi lên Polygon Public Chain (thử nghiệm song song)
            try {
                if (config.getPublicRpcEndpoint() != null && !config.getPublicRpcEndpoint().isBlank()) {
                    String polygonTxHash = polygonClient.sendTransaction(event.getEventData());
                    Anchor polygonAnchor = new Anchor();
                    polygonAnchor.setTxHash(polygonTxHash);
                    polygonAnchor.setStatus("SUBMITTED");
                    polygonAnchor.setChainId(config.getPublicChainId());
                    polygonAnchor.setBlockNumber(polygonClient.getCurrentBlockNumber());
                    polygonAnchor.setConfirmations(0);
                    polygonAnchor.setExplorerUrl("https://amoy.polygonscan.com/tx/" + polygonTxHash);
                    anchorService.persist(polygonAnchor);
                }
            } catch (Exception polyEx) {
                System.err.println("Polygon anchoring warning (non-fatal): " + polyEx.getMessage());
            }

            outboxEventService.complete(event);

            auditLogService.persist(buildAuditLog(
                null,
                "OUTBOX_PROCESS_SUCCESS",
                "outboxEvents",
                event.getId(),
                "{\"txHash\":\"" + txHash + "\",\"batchIdHash\":\"" + event.getBatchIdHash() + "\"}",
                null
            ));

        } catch (Exception e) {
            handleFailure(event, e);
        }
    }

    private void handleFailure(OutboxEvent event, Exception e) {
        String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
        boolean isDeadLetter = (event.getAttempts() + 1) >= 5;

        if (isDeadLetter) {
            outboxEventService.markFailed(event, "DLQ: " + msg);
            auditLogError(event, "DLQ_EXCEEDED_ATTEMPTS: " + msg);
            System.err.println("DLQ ALERT: Event " + event.getId()
                + " for batchIdHash " + event.getBatchIdHash()
                + " permanently failed after " + (event.getAttempts() + 1) + " attempts. Reason: " + msg);
        } else {
            outboxEventService.incrementAttempts(event);
            auditLogError(event, "RETRY_" + (event.getAttempts() + 1) + ": " + msg);
        }
    }

    private void auditLogError(OutboxEvent event, String errorMsg) {
        try {
            auditLogService.persist(buildAuditLog(
                null,
                "OUTBOX_PROCESS_ERROR",
                "outboxEvents",
                event.getId(),
                new AuditError(errorMsg, event.getBatchIdHash(), event.getSeq()).toString(),
                null
            ));
        } catch (Exception ex) { }
    }

    @Override
    @Schedule(second = "0/5", minute = "*", hour = "*", persistent = false, info = "checkConfirmations")
    public void checkConfirmations() {
        try {
            int required = config.getRequiredConfirmations();
            for (Anchor anchor : anchorService.findByStatus("SUBMITTED")) {
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
                        auditLogService.persist(buildAuditLog(
                            null, "ANCHOR_CONFIRMED", "anchors", anchor.getId(), anchor.toString(), null
                        ));
                    }
                    anchorService.persist(anchor);
                } catch (Exception inner) {
                    System.err.println("checkConfirmations: anchor " + anchor.getId() + " failed: " + inner);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in checkConfirmations: " + e.getMessage());
        }
    }

    private AuditLog buildAuditLog(Long actorId, String action, String entity, Long entityId, String detail, String ipAddr) {
        return AuditLog.builder()
                .actor(null)
                .action(action)
                .entity(entity)
                .entityId(entityId)
                .detail(detail)
                .ipAddr(ipAddr)
                .build();
    }
}
