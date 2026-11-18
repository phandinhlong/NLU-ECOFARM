package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;

import java.util.List;
import java.util.Optional;

public interface CultivationEventService {

    List<CultivationEvent> findByBatchIdOrderBySeqAsc(Long batchId);

    /**
     * Finds active CultivationEvent by ID with batch eagerly fetched.
     *
     * @param id event ID
     * @return Optional containing entity if found and active, otherwise Optional.empty()
     */
    Optional<CultivationEvent> findById(Long id);
}
