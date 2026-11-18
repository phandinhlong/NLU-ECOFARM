package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.common.dto.request.CertificateCriteria;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Certificate;

import java.util.List;

public interface CertificateService {

    List<Certificate> findPage(CertificateCriteria criteria);

    Long countActive(CertificateCriteria criteria);

    Boolean existsByCertNo(String certNo);

    Boolean existsByCertNoAndIdNot(String certNo, Long id);

    Certificate findById(Long id);

    Certificate save(Certificate certificate);
}
