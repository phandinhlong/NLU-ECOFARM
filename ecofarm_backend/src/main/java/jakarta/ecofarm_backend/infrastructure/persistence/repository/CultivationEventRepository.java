package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.common.dto.request.EventPageCriteria;
import jakarta.ecofarm_backend.common.enums.EventReviewStatus;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;

import java.util.List;
import java.util.Optional;

public interface CultivationEventRepository {

    List<CultivationEvent> findByBatchIdOrderBySeqAsc(Long batchId);

    List<CultivationEvent> findPage(EventPageCriteria criteria);

    List<CultivationEvent> findReviewPage(Long greenhouseId, EventPageCriteria criteria);

    Long countReviewPage(Long greenhouseId, EventReviewStatus reviewStatus);

    CultivationEvent save(CultivationEvent event);

    Optional<CultivationEvent> findById(Long id);

    Optional<CultivationEvent> findByClientEventId(String clientEventId);
}
