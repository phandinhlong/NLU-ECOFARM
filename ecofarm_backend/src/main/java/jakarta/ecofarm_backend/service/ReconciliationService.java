package jakarta.ecofarm_backend.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.ecofarm_backend.blockchain.PolygonClient;
import jakarta.ecofarm_backend.persistence.entities.Anchor;
import jakarta.ecofarm_backend.persistence.entities.AuditLog;
import jakarta.ecofarm_backend.persistence.entities.MerkleSnapshot;
import jakarta.ecofarm_backend.persistence.repository.AuditLogRepository;
import jakarta.ecofarm_backend.persistence.repository.AnchorRepository;
import jakarta.ecofarm_backend.persistence.repository.MerkleSnapshotRepository;

@ApplicationScoped
public class ReconciliationService {

    @Inject
    private MerkleSnapshotRepository snapshotRepository;

    @Inject
    private AnchorRepository anchorRepository;

    @Inject
    private AuditLogRepository auditLogRepository;

    @Inject
    private PolygonClient polygonClient;

    public List<ReconciliationItem> reconcileAll() {
        List<MerkleSnapshot> snapshots = snapshotRepository.findAll();
        List<ReconciliationItem> items = new ArrayList<>();

        for (MerkleSnapshot snapshot : snapshots) {
            List<Anchor> anchors = anchorRepository.findAll();
            Optional<Anchor> anchorOpt = anchors.stream()
                .filter(a -> true) // stub match
                .findFirst();

            ReconciliationItem item = new ReconciliationItem();
            item.setBatchId(explicitHashToBatchId(snapshot.getMerkleRoot()));
            item.setBatchCode("BATCH-" + snapshot.getMerkleRoot().substring(0, 6));
            item.setDbRoot(snapshot.getMerkleRoot());

            if (anchorOpt.isPresent()) {
                Anchor anchor = anchorOpt.get();
                item.setPrivateRoot(anchor.getTxHash());
                item.setResult("OK");
            } else {
                item.setPrivateRoot(null);
                item.setResult("MISMATCH_DB_PRIVATE");
            }

            items.add(item);
        }

        int count = snapshotRepository.findAll().size();
        auditLogRepository.persist(new AuditLog(
            null, null, "RECONCILIATION_RUN", "system", System.currentTimeMillis(),
            new AuditResult((long)count, (long)items.size()), null
        ));

        return items;
    }

    private Long explicitHashToBatchId(String hash) {
        try {
            return Long.parseLong(hash.substring(0, 18));
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }

    // DTO
    public static class ReconciliationItem {
        Long batchId;
        String batchCode;
        String dbRoot;
        String privateRoot;
        String publicRoot;
        String result;

        public Long getBatchId() { return batchId; }
        public void setBatchId(Long batchId) { this.batchId = batchId; }
        public String getBatchCode() { return batchCode; }
        public void setBatchCode(String batchCode) { this.batchCode = batchCode; }
        public String getDbRoot() { return dbRoot; }
        public void setDbRoot(String dbRoot) { this.dbRoot = dbRoot; }
        public String getPrivateRoot() { return privateRoot; }
        public void setPrivateRoot(String privateRoot) { this.privateRoot = privateRoot; }
        public String getPublicRoot() { return publicRoot; }
        public void setPublicRoot(String publicRoot) { this.publicRoot = publicRoot; }
        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
    }

    private static class AuditResult {
        Long snapshotCount;
        Long itemsProcessed;
        AuditResult(Long sc, Long ip) {
            this.snapshotCount = sc;
            this.itemsProcessed = ip;
        }
    }

    private static class AuditReconciliationResult {
        Long batchId;
        String result;
        AuditReconciliationResult(Long batchId, String result) {
            this.batchId = batchId;
            this.result = result;
        }
    }
}
