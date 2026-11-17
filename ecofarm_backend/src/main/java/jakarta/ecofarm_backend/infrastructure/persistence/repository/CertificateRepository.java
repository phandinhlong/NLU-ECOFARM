package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.common.dto.request.CertificateCriteria;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Certificate;

import java.util.List;

public interface CertificateRepository {

    List<Certificate> findPage(CertificateCriteria criteria);

    long countActive(CertificateCriteria criteria);

    boolean existsByCertNo(String certNo);

    Certificate save(Certificate certificate);
}
