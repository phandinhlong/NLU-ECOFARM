package jakarta.ecofarm_backend.modules.blockchain.service;

import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import java.util.List;
import java.util.concurrent.Semaphore;

import jakarta.ecofarm_backend.modules.blockchain.BesuClient;
import jakarta.ecofarm_backend.modules.blockchain.PolygonClient;
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

    @Inject
    private PolygonClient polygonClient;

    // Rate limiter: max 2 blockchain transactions per second (as per FR-LDG-006)
    private final Semaphore rateLimiter = new Semaphore(2);

//    @Schedule(second = "*/1", minute = "*", hour = "*", persistent = false)
//    public void processOutbox() {
//        try {
//            List<OutboxEvent> pending = outboxEventRepository.findPending();
//            int processed = 0;
//            for (OutboxEvent event : pending) {
//                if (processed >= 2) break; // respect rate limit
//                try {
//                    // Rate limiting
//                    rateLimiter.acquire();
//
//                    // Idempotency check: if already processed by another thread, skip
//                    if (outboxEventRepository.existsProcessedByBatchIdHashAndSeq(event.getBatchIdHash(), event.getSeq())) {
//                        outboxEventRepository.complete(event);
//                        processed++;
//                        continue;
//                    }
//                    // Send to private chain (Besu)
//                    String PrivateTxHash = besuClient.sendTransaction(event.getEventData());
//                    if (PrivateTxHash != null && !PrivateTxHash.isEmpty()) {
//                        // Create anchor record for private chain
//                        Anchor privateAnchor = new Anchor();
//                        // Add anchor details (snapshotId, chainId, contractAddress)
//                        privateAnchor.setChainId(Integer.valueOf(config.getPrivateNetworkId()));
//                        privateAnchor.setStatus("SUCCESS");
//                        privateAnchor.setTxHash(PrivateTxHash);
//privateAnchor.setAnchoredAt(System.currentTimeMillis() / 1000);
//                        anchorRepository.persist(privateAnchor);
//
//                        // Audit log
//                        AuditLog privateLog = new AuditLog(
//                            null, // actorId
//                            null, // actorName
//                            "OUTBOX_EVENT_ANCHORED",
//                            "outboxEvents",
//                            event.getId(),
//                            privateAnchor.toString(),
//                            null
//                        );
//                        auditLogRepository.persist(privateLog);
//                        String publicTxHash = polygonClient.sendTransaction(event.getEventData());
//                        if(publicTxHash != null && !publicTxHash.isEmpty()){
//                            Anchor publicAnchor = new Anchor();
//                            publicAnchor.setChainId(config.getPublicChainId());
//                            publicAnchor.setStatus("SUCCESS");
//                            publicAnchor.setTxHash(publicTxHash);
//                            publicAnchor.setExplorerUrl("https://amoy.polygonscan.com/tx/"+publicTxHash);
//                            publicAnchor.setAnchoredAt(System.currentTimeMillis() / 1000);
//                            anchorRepository.persist(publicAnchor);
//
//                            // Audit log public chain
//                            AuditLog publicLog = new AuditLog(
//                                    null,
//                                    null,
//                                    "OUTBOX_EVENT_ANCHORED_PUBLIC",
//                                    "outboxEvents",
//                                    event.getId(),
//                                    publicAnchor.toString(),
//                                    null
//                            );
//                            auditLogRepository.persist(publicLog);
//                        }
//
//                        // Mark outbox as processed
//                        outboxEventRepository.complete(event);
//                        processed++;
//                    } else {
//                        outboxEventRepository.markFailed(event, "Invalid response from private chain");
//                    }
//                } catch (Exception e) {
//                    outboxEventRepository.incrementAttempts(event);
//                    // Audit error
//                    auditLogError(event, e.getMessage());
//                } finally {
//                    rateLimiter.release();
//                }
//            }
//        } catch (Exception e) {
//            // Log error
//            System.err.println("Error in processOutbox: " + e.getMessage());
//        }
//    }
//    @Schedule(second = "*/2", minute = "*", hour = "*", persistent = false)
    public void processOutbox() {
        try {
            List<OutboxEvent> pending = outboxEventRepository.findPending();
            int processed = 0;
            for (OutboxEvent event : pending) {
                if (processed >= 2) break; // Giới hạn rate limit
                try {
                    rateLimiter.acquire();
                    // Idempotency check: nếu đã xử lý thì complete và bỏ qua
                    if (outboxEventRepository.existsProcessedByBatchIdHashAndSeq(event.getBatchIdHash(), event.getSeq())) {
                        outboxEventRepository.complete(event);
                        processed++;
                        continue;
                    }
                    // 1. TÁCH BESU: Thử gửi Besu, nếu node tắt/lỗi thì bỏ qua, KHÔNG làm dừng tiến trình
                    try {
                        String privateTxHash = besuClient.sendTransaction(event.getEventData());
                        if (privateTxHash != null && !privateTxHash.isEmpty()) {
                            Anchor privateAnchor = new Anchor();
                            privateAnchor.setChainId(Integer.valueOf(config.getPrivateNetworkId()));
                            privateAnchor.setStatus("SUCCESS");
                            privateAnchor.setTxHash(privateTxHash);
                            privateAnchor.setAnchoredAt(System.currentTimeMillis() / 1000);
                            privateAnchor.setCreatedAt(System.currentTimeMillis());
                            privateAnchor.setUpdatedAt(System.currentTimeMillis());
                            anchorRepository.persist(privateAnchor);
                            // Audit log cho Besu
                            AuditLog privateLog = new AuditLog(
                                    null, null,
                                    "OUTBOX_EVENT_ANCHORED",
                                    "outboxEvents",
                                    event.getId(),
                                    privateAnchor.toString(),
                                    null
                            );
                            auditLogRepository.persist(privateLog);
                        }
                    } catch (Exception besuEx) {
                        System.out.println("[WARN] Besu bo qua do khong bat node: " + besuEx.getMessage());
                    }
                    // 2. NEO THẲNG LÊN PUBLIC BLOCKCHAIN (POLYGON AMOY)
                    System.out.println("[INFO] Dang gui transaction len Polygon Amoy cho event id: " + event.getId());
                    String publicTxHash = polygonClient.sendTransaction(event.getEventData());
                    if (publicTxHash != null && !publicTxHash.isEmpty()) {
                        System.out.println("[SUCCESS] Da neo len Polygon! txHash: " + publicTxHash);
                        Anchor publicAnchor = new Anchor();
                        publicAnchor.setChainId(config.getPublicChainId());
                        publicAnchor.setStatus("SUCCESS");
                        publicAnchor.setTxHash(publicTxHash);
                        publicAnchor.setExplorerUrl("https://amoy.polygonscan.com/tx/" + publicTxHash);
                        publicAnchor.setAnchoredAt(System.currentTimeMillis() / 1000);
                        publicAnchor.setCreatedAt(System.currentTimeMillis());
                        publicAnchor.setUpdatedAt(System.currentTimeMillis());
                        anchorRepository.persist(publicAnchor);
                        // Audit log cho Polygon
                        AuditLog publicLog = new AuditLog(
                                null, null,
                                "OUTBOX_EVENT_ANCHORED_PUBLIC",
                                "outboxEvents",
                                event.getId(),
                                publicAnchor.toString(),
                                null
                        );
                        auditLogRepository.persist(publicLog);
                        // Đánh dấu event đã xử lý thành công
                        outboxEventRepository.complete(event);
                        processed++;
                    } else {
                        outboxEventRepository.markFailed(event, "Khong nhan duoc txHash tu Polygon");
                    }
                } catch (Exception e) {
                    System.err.println("[ERROR] Loi khi neo Polygon: " + e.getMessage());
                    outboxEventRepository.incrementAttempts(event);
                    auditLogError(event, e.getMessage());
                } finally {
                    rateLimiter.release();
                }
            }
        } catch (Exception e) {
            System.err.println("Error in processOutbox: " + e.getMessage());
        }
    }

    // Theo dõi anchor đã gửi lên Polygon; CONFIRMED khi đủ số confirmation
    private void checkPublicConfirmation(Anchor anchor) throws Exception {
        if (anchor.getTxHash() == null) return;
        int confirmations = polygonClient.getConfirmations(anchor.getTxHash());
        anchor.setConfirmations(confirmations);
        if (anchor.getBlockNumber() == null) {
            anchor.setBlockNumber(polygonClient.getCurrentBlockNumber());
        }
        if (confirmations >= config.getRequiredConfirmations()) {
            anchor.setStatus("CONFIRMED");
            auditLogRepository.persist(new AuditLog(
                null, null, "PUBLIC_ANCHOR_CONFIRMED", "anchors", anchor.getId(), anchor.toString(), null
            ));
        }
        anchorRepository.persist(anchor);
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
