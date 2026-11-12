package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Certificate;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Greenhouse;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.ProductPackage;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.QcRecord;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.QrCode;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Variety;

@Stateless
public class TraceLookupRepository {

    @PersistenceContext
    private EntityManager em;

    public Optional<QrCode> findQrCodeByCode(String code) {
        return em.createQuery("SELECT q FROM QrCode q WHERE q.code = :code", QrCode.class)
                .setParameter("code", code)
                .getResultStream()
                .findFirst();
    }

    public Optional<ProductPackage> findPackageById(Long id) {
        return Optional.ofNullable(em.find(ProductPackage.class, id));
    }

    public Optional<Greenhouse> findGreenhouseById(Long id) {
        return Optional.ofNullable(em.find(Greenhouse.class, id));
    }

    public Optional<Variety> findVarietyById(Long id) {
        return Optional.ofNullable(em.find(Variety.class, id));
    }

    public List<QcRecord> findQcRecordsByBatchId(Long batchId) {
        return em.createQuery(
                        "SELECT q FROM QcRecord q WHERE q.batchId = :batchId ORDER BY q.inspectedAt DESC, q.id DESC",
                        QcRecord.class)
                .setParameter("batchId", batchId)
                .getResultList();
    }

    public List<Certificate> findCertificatesByGreenhouseId(Long greenhouseId) {
        return em.createQuery(
                        "SELECT c FROM Certificate c WHERE c.greenhouseId = :greenhouseId ORDER BY c.id",
                        Certificate.class)
                .setParameter("greenhouseId", greenhouseId)
                .getResultList();
    }
}
