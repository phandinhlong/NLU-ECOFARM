package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.common.dto.request.EventPageCriteria;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.CultivationEventRepository;
import jakarta.ecofarm_backend.infrastructure.service.CultivationEventService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CultivationEventServiceImpl implements CultivationEventService {

    @Inject
    private CultivationEventRepository cultivationEventRepository;

    @Override
    public List<CultivationEvent> findByBatchIdOrderBySeqAsc(Long batchId) {
        return this.cultivationEventRepository.findByBatchIdOrderBySeqAsc(batchId);
    }

    @Override
    public List<CultivationEvent> findPage(EventPageCriteria criteria) {
        return this.cultivationEventRepository.findPage(criteria);
    }

    @Override
    public CultivationEvent save(CultivationEvent event) {
        return this.cultivationEventRepository.save(event);
    }

    @Override
    public Optional<CultivationEvent> findById(Long id) {
        return this.cultivationEventRepository.findById(id);
    }

    @Override
    public Optional<CultivationEvent> findByClientEventId(String clientEventId) {
        return this.cultivationEventRepository.findByClientEventId(clientEventId);
    }
}
