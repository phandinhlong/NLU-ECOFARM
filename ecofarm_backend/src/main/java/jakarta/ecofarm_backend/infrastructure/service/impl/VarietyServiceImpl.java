package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.common.dto.request.VarietyCriteria;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Variety;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.VarietyRepository;
import jakarta.ecofarm_backend.infrastructure.service.VarietyService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class VarietyServiceImpl implements VarietyService {

    @Inject
    private VarietyRepository varietyRepository;

    @Override
    public List<Variety> findPage(VarietyCriteria criteria) {
        return this.varietyRepository.findPage(criteria);
    }

    @Override
    public Long countActive(VarietyCriteria criteria) {
        return this.varietyRepository.countActive(criteria);
    }

    @Override
    public Map<Long, Long> countBatchesByVarietyIds(List<Long> varietyIds) {
        return this.varietyRepository.countBatchesByVarietyIds(varietyIds);
    }

    @Override
    public Variety findById(Long id) {
        return this.varietyRepository.findById(id);
    }

    @Override
    public Boolean existsByCode(String code) {
        return this.varietyRepository.existsByCode(code);
    }

    @Override
    public Boolean existsByCodeAndIdNot(String code, Long id) {
        return this.varietyRepository.existsByCodeAndIdNot(code, id);
    }

    @Override
    public Long countBatches(Long varietyId) {
        return this.varietyRepository.countBatches(varietyId);
    }

    @Override
    public Variety save(Variety variety) {
        return this.varietyRepository.save(variety);
    }
}
