package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.common.dto.request.EventPageCriteria;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;

import java.util.List;
import java.util.Optional;

public interface CultivationEventService {

    void persist(CultivationEvent event);

    CultivationEvent merge(CultivationEvent event);

    CultivationEvent findById(Long id);

    CultivationEvent findByClientEventId(String clientEventId);

    Integer findNextSeqByBatchId(Long batchId);

    List<CultivationEvent> findByBatchIdOrderBySeqAsc(Long batchId);

    List<CultivationEvent> findPage(EventPageCriteria criteria);

    CultivationEvent save(CultivationEvent event);

    Optional<CultivationEvent> findById(Long id);

    Optional<CultivationEvent> findByClientEventId(String clientEventId);
}
