package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.common.dto.request.CertificateCriteria;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Certificate;

import java.util.List;

public interface CertificateRepository {

    List<Certificate> findPage(CertificateCriteria criteria);

    Long countActive(CertificateCriteria criteria);

    boolean existsByCertNo(String certNo);

    boolean existsByCertNoAndIdNot(String certNo, Long id);

    Certificate findById(Long id);

    Certificate save(Certificate certificate);
}
