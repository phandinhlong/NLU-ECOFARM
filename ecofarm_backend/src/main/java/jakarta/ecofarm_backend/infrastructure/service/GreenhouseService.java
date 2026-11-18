package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.common.dto.request.GreenhouseCriteria;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Greenhouse;

import java.util.List;
import java.util.Map;

public interface GreenhouseService {

    List<Greenhouse> findPage(GreenhouseCriteria criteria);

    Long countActive(GreenhouseCriteria criteria);

    Map<Long, Long> countBatchesByGreenhouseIds(List<Long> greenhouseIds);

    Greenhouse findById(Long id);

    Boolean existsByCode(String code);

    Boolean existsByCodeAndIdNot(String code, Long id);

    Long countBatches(Long greenhouseId);

    Long countCertificates(Long greenhouseId);

    void delete(Greenhouse greenhouse);

    Greenhouse save(Greenhouse greenhouse);
}
