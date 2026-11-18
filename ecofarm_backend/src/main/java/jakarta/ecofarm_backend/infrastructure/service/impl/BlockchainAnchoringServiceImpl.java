package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;

import java.util.List;
import java.util.concurrent.Semaphore;

import jakarta.ecofarm_backend.common.dto.request.AuditLogParam;
import jakarta.ecofarm_backend.common.dto.response.AuditError;
import jakarta.ecofarm_backend.common.enums.AnchorEvent;
import jakarta.ecofarm_backend.common.enums.AnchorStatus;
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

            Anchor anchor = Anchor.builder()
                    .txHash(txHash)
                    .status(AnchorStatus.SUBMITTED.name())
                    .blockNumber(besuClient.getCurrentBlockNumber())
                    .confirmations(0)
                    .explorerUrl("http://localhost:8545/tx/" + txHash)
                    .build();
            anchorService.persist(anchor);

            // Gửi lên Polygon Public Chain (thử nghiệm song song)
            try {
                if (config.getPublicRpcEndpoint() != null && !config.getPublicRpcEndpoint().isBlank()) {
                    String polygonTxHash = polygonClient.sendTransaction(event.getEventData());
                    Anchor polygonAnchor = Anchor.builder()
                            .txHash(polygonTxHash)
                            .status(AnchorStatus.SUBMITTED.name())
                            .chainId(config.getPublicChainId())
                            .blockNumber(polygonClient.getCurrentBlockNumber())
                            .confirmations(0)
                            .explorerUrl("https://amoy.polygonscan.com/tx/" + polygonTxHash)
                            .build();
                    anchorService.persist(polygonAnchor);
                }
            } catch (Exception polyEx) {
                System.err.println("Polygon anchoring warning (non-fatal): " + polyEx.getMessage());
            }

            outboxEventService.complete(event);

            auditLogService.persist(buildAuditLog(AuditLogParam.builder()
                .action("OUTBOX_PROCESS_SUCCESS")
                .entity("outboxEvents")
                .entityId(event.getId())
                .detail("{\"txHash\":\"" + txHash + "\",\"batchIdHash\":\"" + event.getBatchIdHash() + "\"}")
                .build()));

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
            AuditError auditError = AuditError.builder()
                    .message(errorMsg)
                    .batchIdHash(event.getBatchIdHash())
                    .seq(event.getSeq())
                    .build();
            auditLogService.persist(buildAuditLog(AuditLogParam.builder()
                .action("OUTBOX_PROCESS_ERROR")
                .entity("outboxEvents")
                .entityId(event.getId())
                .detail(auditError.toString())
                .build()));
        } catch (Exception ex) {

        }
    }

    @Override
    @Schedule(second = "0/5", minute = "*", hour = "*", persistent = false, info = "checkConfirmations")
    public void checkConfirmations() {
        try {
            int required = config.getRequiredConfirmations();
            for (Anchor anchor : anchorService.findByStatus(AnchorStatus.SUBMITTED.name())) {
                if (anchor.getTxHash() == null) continue;
                try {
                    int confirmations = besuClient.getConfirmations(anchor.getTxHash());
                    anchor.setConfirmations(confirmations);
                    if (anchor.getBlockNumber() == null) {
                        anchor.setBlockNumber(besuClient.getCurrentBlockNumber());
                    }
                    if (confirmations >= required) {
                        AnchorStatus current = AnchorStatus.fromString(anchor.getStatus());
                        anchor.setStatus(current.canFire(AnchorEvent.CONFIRM)
                                ? current.fire(AnchorEvent.CONFIRM).name()
                                : AnchorStatus.CONFIRMED.name());
                        anchor.setAnchoredAt(System.currentTimeMillis());
                        auditLogService.persist(buildAuditLog(AuditLogParam.builder()
                            .action("ANCHOR_CONFIRMED")
                            .entity("anchors")
                            .entityId(anchor.getId())
                            .detail(anchor.toString())
                            .build()));
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

    private AuditLog buildAuditLog(AuditLogParam param) {
        if (param == null) return null;
        return AuditLog.builder()
                .action(param.getAction())
                .entity(param.getEntity())
                .entityId(param.getEntityId())
                .detail(param.getDetail())
                .ipAddr(param.getIpAddr())
                .build();
    }
}
