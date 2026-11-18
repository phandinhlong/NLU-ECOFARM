package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.MerkleSnapshot;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.MerkleSnapshotRepository;
import jakarta.ecofarm_backend.infrastructure.service.MerkleSnapshotService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class MerkleSnapshotServiceImpl implements MerkleSnapshotService {

    @Inject
    private MerkleSnapshotRepository merkleSnapshotRepository;

    @Override
    public void persist(MerkleSnapshot snapshot) {
        this.merkleSnapshotRepository.persist(snapshot);
    }

    @Override
    public void updateLeavesJson(Long snapshotId, String leavesJson) {
        this.merkleSnapshotRepository.updateLeavesJson(snapshotId, leavesJson);
    }

    @Override
    public MerkleSnapshot findById(Long id) {
        return this.merkleSnapshotRepository.findById(id);
    }

    @Override
    public List<MerkleSnapshot> findAll() {
        return this.merkleSnapshotRepository.findAll();
    }

    @Override
    public MerkleSnapshot findByBatchId(Long batchId) {
        return this.merkleSnapshotRepository.findByBatchId(batchId);
    }
}
