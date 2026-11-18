package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;

import java.util.List;
import java.util.Optional;

public interface CultivationEventService {

    List<CultivationEvent> findByBatchIdOrderBySeqAsc(Long batchId);

    CultivationEvent save(CultivationEvent event);

    Optional<CultivationEvent> findById(Long id);

    Optional<CultivationEvent> findByClientEventId(String clientEventId);
}
