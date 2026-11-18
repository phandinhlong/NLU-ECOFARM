package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;

import java.util.List;

public interface AnchorService {

    void persist(Anchor anchor);

    Anchor findById(Long id);

    List<Anchor> findByStatus(String status);

    List<Anchor> findAll();

    List<Anchor> findBySnapshotId(Long snapshotId);
}
