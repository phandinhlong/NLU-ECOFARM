package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.common.dto.request.CertificateCriteria;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Certificate;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.CertificateRepository;
import jakarta.ecofarm_backend.infrastructure.service.CertificateService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class CertificateServiceImpl implements CertificateService {

    @Inject
    private CertificateRepository certificateRepository;

    @Override
    public List<Certificate> findPage(CertificateCriteria criteria) {
        return this.certificateRepository.findPage(criteria);
    }

    @Override
    public Long countActive(CertificateCriteria criteria) {
        return this.certificateRepository.countActive(criteria);
    }

    @Override
    public Boolean existsByCertNo(String certNo) {
        return this.certificateRepository.existsByCertNo(certNo);
    }

    @Override
    public Boolean existsByCertNoAndIdNot(String certNo, Long id) {
        return this.certificateRepository.existsByCertNoAndIdNot(certNo, id);
    }

    @Override
    public Certificate findById(Long id) {
        return this.certificateRepository.findById(id);
    }

    @Override
    public Certificate save(Certificate certificate) {
        return this.certificateRepository.save(certificate);
    }
}
