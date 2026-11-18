package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import jakarta.validation.constraints.Positive;

import java.util.List;

public interface AnchorService {

    void persist(Anchor anchor);

    Anchor findById(@Positive Long id);

    List<Anchor> findByStatus(String status);

    List<Anchor> findAll();

    List<Anchor> findBySnapshotId(@Positive Long snapshotId);
}
