package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.common.dto.request.GreenhouseCriteria;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Greenhouse;

import java.util.List;
import java.util.Map;

public interface GreenhouseRepository {

    List<Greenhouse> findPage(GreenhouseCriteria criteria);

    long countActive(GreenhouseCriteria criteria);

    Map<Long, Long> countBatchesByGreenhouseIds(List<Long> greenhouseIds);

    Greenhouse findById(Long id);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    long countBatches(Long greenhouseId);

    Greenhouse save(Greenhouse greenhouse);
}
