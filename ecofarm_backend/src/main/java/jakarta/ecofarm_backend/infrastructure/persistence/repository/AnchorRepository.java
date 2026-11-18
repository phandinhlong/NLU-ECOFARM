package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;

import java.util.List;

public interface AnchorRepository {
    void persist(Anchor anchor);

    Anchor findById(Long id);

    List<Anchor> findByStatus(String status);

    List<Anchor> findAll();

    List<Anchor> findBySnapshotId(Long snapshotId);
}
