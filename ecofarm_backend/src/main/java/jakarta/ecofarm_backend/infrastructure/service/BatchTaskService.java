package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.common.enums.TaskStatus;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.BatchTask;

import java.util.List;
import java.util.Optional;

public interface BatchTaskService {

    BatchTask save(BatchTask task);

    Optional<BatchTask> findById(Long id);

    List<BatchTask> findPage(Long batchId, Long lastIndex, TaskStatus status, int limit);
}
