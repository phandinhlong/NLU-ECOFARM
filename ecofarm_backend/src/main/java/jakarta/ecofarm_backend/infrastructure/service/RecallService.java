package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Recall;
import jakarta.validation.constraints.Positive;

import java.util.List;

public interface RecallService {

    void persist(Recall recall);

    Recall findById(@Positive Long id);

    List<Recall> findByBatchId(@Positive Long batchId);
}
