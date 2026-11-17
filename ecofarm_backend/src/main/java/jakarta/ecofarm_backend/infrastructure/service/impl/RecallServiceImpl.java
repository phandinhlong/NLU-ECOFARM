package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Recall;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.RecallRepository;
import jakarta.ecofarm_backend.infrastructure.service.RecallService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class RecallServiceImpl implements RecallService {

    @Inject
    private RecallRepository recallRepository;

    @Override
    public void persist(Recall recall) {
        this.recallRepository.persist(recall);
    }

    @Override
    public Recall findById(Long id) {
        return this.recallRepository.findById(id);
    }

    @Override
    public List<Recall> findByBatchId(Long batchId) {
        return this.recallRepository.findByBatchId(batchId);
    }
}
