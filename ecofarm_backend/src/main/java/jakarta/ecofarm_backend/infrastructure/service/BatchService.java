package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;

public interface BatchService {

    Batch findById(Long id);

    Batch findByBatchCode(String batchCode);

    Batch merge(Batch batch);
}
