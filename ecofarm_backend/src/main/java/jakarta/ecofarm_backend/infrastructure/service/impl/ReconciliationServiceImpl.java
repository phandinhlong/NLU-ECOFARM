package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.ecofarm_backend.common.dto.response.ReconciliationItem;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.MerkleSnapshot;
import jakarta.ecofarm_backend.infrastructure.service.AnchorService;
import jakarta.ecofarm_backend.infrastructure.service.MerkleSnapshotService;
import jakarta.ecofarm_backend.infrastructure.service.ReconciliationService;

@ApplicationScoped
public class ReconciliationServiceImpl implements ReconciliationService {

    @Inject
    private MerkleSnapshotService snapshotService;

    @Inject
    private AnchorService anchorService;

    @Override
    public List<ReconciliationItem> reconcileAll() {
        List<MerkleSnapshot> snapshots = snapshotService.findAll();
        List<ReconciliationItem> items = new ArrayList<>();

        for (MerkleSnapshot snapshot : snapshots) {
            List<Anchor> anchors = anchorService.findAll();
            Optional<Anchor> anchorOpt = anchors.stream()
                .filter(a -> snapshot.getId() != null && a.getSnapshot() != null
                    && snapshot.getId().equals(a.getSnapshot().getId()))
                .findFirst();

            String privateRoot = anchorOpt.map(Anchor::getTxHash).orElse(null);
            String result = anchorOpt.isPresent() ? "OK" : "MISMATCH_DB_PRIVATE";

            ReconciliationItem item = ReconciliationItem.builder()
                    .batchId(snapshot.getBatch() != null ? snapshot.getBatch().getId() : null)
                    .batchCode(snapshot.getBatch() != null ? snapshot.getBatch().getBatchCode() : "BATCH-?")
                    .dbRoot(snapshot.getMerkleRoot())
                    .privateRoot(privateRoot)
                    .result(result)
                    .build();

            items.add(item);
        }

        return items;
    }
}
