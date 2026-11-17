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
    public List<CultivationEvent> findByBatchIdOrderBySeqAsc(Long batchId) {
        return this.cultivationEventRepository.findByBatchIdOrderBySeqAsc(batchId);
    }
}
