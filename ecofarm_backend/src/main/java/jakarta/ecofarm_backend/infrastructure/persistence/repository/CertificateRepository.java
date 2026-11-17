package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Certificate;

import java.util.List;

public interface CertificateRepository {

    List<Certificate> findPage(int page, int size, Long greenhouseId, Integer expiringInDays);

    long countActive(Long greenhouseId, Integer expiringInDays);

    boolean existsByCertNo(String certNo);

    void persist(Certificate certificate);
}
