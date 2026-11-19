package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.common.enums.TaskStatus;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.BatchTask;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.BatchTaskRepository;
import jakarta.ecofarm_backend.infrastructure.service.BatchTaskService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class BatchTaskServiceImpl implements BatchTaskService {

    @Inject
    private BatchTaskRepository batchTaskRepository;

    @Override
    public BatchTask save(BatchTask task) {
        return this.batchTaskRepository.save(task);
    }

    @Override
    public Optional<BatchTask> findById(Long id) {
        return this.batchTaskRepository.findById(id);
    }

    @Override
    public List<BatchTask> findPage(Long batchId, Long lastIndex, TaskStatus status, int limit) {
        return this.batchTaskRepository.findPageByBatchId(batchId, lastIndex, status, limit);
    }
}
