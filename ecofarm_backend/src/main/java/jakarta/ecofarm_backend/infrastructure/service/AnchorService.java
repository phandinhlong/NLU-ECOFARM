package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.common.dto.response.PageResponse;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public interface AnchorService {

    void persist(Anchor anchor);

    Anchor findById(@Positive Long id);

    List<Anchor> findByStatus(String status);

    PageResponse<Anchor> findPage(@PositiveOrZero int page, @Positive int size);

    List<Anchor> findBySnapshotId(@Positive Long snapshotId);
}
