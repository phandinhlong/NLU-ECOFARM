package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.CultivationEventRepository;
import jakarta.ecofarm_backend.infrastructure.service.CultivationEventService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class CultivationEventServiceImpl implements CultivationEventService {

    @Inject
    private CultivationEventRepository cultivationEventRepository;

    @Override
    public void persist(CultivationEvent event) {
        this.cultivationEventRepository.persist(event);
    }

    @Override
    public CultivationEvent merge(CultivationEvent event) {
        return this.cultivationEventRepository.merge(event);
    }

    @Override
    public CultivationEvent findById(Long id) {
        return this.cultivationEventRepository.findById(id);
    }

    @Override
    public CultivationEvent findByClientEventId(String clientEventId) {
        return this.cultivationEventRepository.findByClientEventId(clientEventId);
    }

    @Override
    public Integer findNextSeqByBatchId(Long batchId) {
        return this.cultivationEventRepository.findNextSeqByBatchId(batchId);
    }

    @Override
    public List<CultivationEvent> findByBatchIdOrderBySeqAsc(Long batchId) {
        return this.cultivationEventRepository.findByBatchIdOrderBySeqAsc(batchId);
    }
}
