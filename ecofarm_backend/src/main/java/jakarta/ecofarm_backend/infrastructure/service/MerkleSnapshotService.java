package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.MerkleSnapshot;
import jakarta.validation.constraints.Positive;

import java.util.List;

public interface MerkleSnapshotService {

    void persist(MerkleSnapshot snapshot);

    void updateLeavesJson(@Positive Long snapshotId, String leavesJson);

    MerkleSnapshot findById(@Positive Long id);

    List<MerkleSnapshot> findAll();

    MerkleSnapshot findByBatchId(@Positive Long batchId);
}
