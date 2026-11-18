package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.common.dto.request.VarietyCriteria;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Variety;

import java.util.List;
import java.util.Map;

public interface VarietyService {

    List<Variety> findPage(VarietyCriteria criteria);

    Long countActive(VarietyCriteria criteria);

    Map<Long, Long> countBatchesByVarietyIds(List<Long> varietyIds);

    Variety findById(Long id);

    Boolean existsByCode(String code);

    Boolean existsByCodeAndIdNot(String code, Long id);

    Long countBatches(Long varietyId);

    Variety save(Variety variety);
}
