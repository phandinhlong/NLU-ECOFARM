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

            ReconciliationItem item = new ReconciliationItem();
            item.setBatchId(snapshot.getBatch() != null ? snapshot.getBatch().getId() : null);
            item.setBatchCode(snapshot.getBatch() != null ? snapshot.getBatch().getBatchCode() : "BATCH-?");
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
}
