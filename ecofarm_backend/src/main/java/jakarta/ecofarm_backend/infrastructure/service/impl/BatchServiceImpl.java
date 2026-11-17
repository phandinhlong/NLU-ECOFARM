package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.BatchRepository;
import jakarta.ecofarm_backend.infrastructure.service.BatchService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class BatchServiceImpl implements BatchService {

    @Inject
    private BatchRepository batchRepository;

    @Override
    public Batch findById(Long id) {
        return this.batchRepository.findById(id);
    }

    @Override
    public Batch findByBatchCode(String batchCode) {
        return this.batchRepository.findByBatchCode(batchCode);
    }

    @Override
    public Batch merge(Batch batch) {
        return this.batchRepository.merge(batch);
    }
}
