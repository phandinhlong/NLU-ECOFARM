package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;

import java.util.List;

public interface CultivationEventService {

    List<CultivationEvent> findByBatchIdOrderBySeqAsc(Long batchId);
}
