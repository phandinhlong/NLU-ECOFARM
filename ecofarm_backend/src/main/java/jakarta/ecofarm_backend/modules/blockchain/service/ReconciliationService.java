package jakarta.ecofarm_backend.modules.blockchain.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.ecofarm_backend.modules.blockchain.PolygonClient;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.MerkleSnapshot;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.AuditLogRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.AnchorRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.MerkleSnapshotRepository;
import jakarta.ecofarm_backend.common.dto.ReconciliationItem;

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

        return items;
    }

    private Long explicitHashToBatchId(String hash) {
        try {
            return Long.parseLong(hash.substring(0, 18));
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }

}
