package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;

import java.util.List;

public interface CultivationEventService {

    void persist(CultivationEvent event);

    CultivationEvent merge(CultivationEvent event);

    CultivationEvent findById(Long id);

    CultivationEvent findByClientEventId(String clientEventId);

    Integer findNextSeqByBatchId(Long batchId);

    List<CultivationEvent> findByBatchIdOrderBySeqAsc(Long batchId);
}
