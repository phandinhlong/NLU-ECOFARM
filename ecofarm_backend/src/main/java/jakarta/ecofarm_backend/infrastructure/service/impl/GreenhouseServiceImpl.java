package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.common.dto.request.GreenhouseCriteria;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Greenhouse;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.GreenhouseRepository;
import jakarta.ecofarm_backend.infrastructure.service.GreenhouseService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class GreenhouseServiceImpl implements GreenhouseService {

    @Inject
    private GreenhouseRepository greenhouseRepository;

    @Override
    public List<Greenhouse> findPage(GreenhouseCriteria criteria) {
        return this.greenhouseRepository.findPage(criteria);
    }

    @Override
    public Long countActive(GreenhouseCriteria criteria) {
        return this.greenhouseRepository.countActive(criteria);
    }

    @Override
    public Map<Long, Long> countBatchesByGreenhouseIds(List<Long> greenhouseIds) {
        return this.greenhouseRepository.countBatchesByGreenhouseIds(greenhouseIds);
    }

    @Override
    public Greenhouse findById(Long id) {
        return this.greenhouseRepository.findById(id);
    }

    @Override
    public Boolean existsByCode(String code) {
        return this.greenhouseRepository.existsByCode(code);
    }

    @Override
    public Boolean existsByCodeAndIdNot(String code, Long id) {
        return this.greenhouseRepository.existsByCodeAndIdNot(code, id);
    }

    @Override
    public Long countBatches(Long greenhouseId) {
        return this.greenhouseRepository.countBatches(greenhouseId);
    }

    @Override
    public Long countCertificates(Long greenhouseId) {
        return this.greenhouseRepository.countCertificates(greenhouseId);
    }

    @Override
    public void delete(Greenhouse greenhouse) {
        this.greenhouseRepository.delete(greenhouse);
    }

    @Override
    public Greenhouse save(Greenhouse greenhouse) {
        return this.greenhouseRepository.save(greenhouse);
    }
}
