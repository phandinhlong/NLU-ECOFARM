package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;

import java.util.List;
import java.util.Optional;

public interface CultivationEventRepository {

    List<CultivationEvent> findByBatchIdOrderBySeqAsc(Long batchId);

    CultivationEvent save(CultivationEvent event);

    Optional<CultivationEvent> findById(Long id);

    Optional<CultivationEvent> findByClientEventId(String clientEventId);
}
