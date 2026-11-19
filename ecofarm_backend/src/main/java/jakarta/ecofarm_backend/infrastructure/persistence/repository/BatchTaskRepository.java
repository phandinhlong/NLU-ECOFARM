package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.common.enums.TaskStatus;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.BatchTask;

import java.util.List;
import java.util.Optional;

public interface BatchTaskRepository {

    BatchTask save(BatchTask task);

    Optional<BatchTask> findById(Long id);

    List<BatchTask> findPageByBatchId(Long batchId, Long lastIndex, TaskStatus status, int limit);
}
