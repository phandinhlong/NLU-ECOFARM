package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.MerkleSnapshot;

import java.util.List;

public interface MerkleSnapshotService {

    void persist(MerkleSnapshot snapshot);

    void updateLeavesJson(Long snapshotId, String leavesJson);

    MerkleSnapshot findById(Long id);

    List<MerkleSnapshot> findAll();

    MerkleSnapshot findByBatchId(Long batchId);
}
