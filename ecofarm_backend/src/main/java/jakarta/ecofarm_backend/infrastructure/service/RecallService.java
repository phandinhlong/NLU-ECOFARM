package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Recall;

import java.util.List;

public interface RecallService {

    void persist(Recall recall);

    Recall findById(Long id);

    List<Recall> findByBatchId(Long batchId);
}
