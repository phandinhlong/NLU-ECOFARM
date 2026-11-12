package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Certificate;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.EventAttachment;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Greenhouse;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.ProductPackage;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.QcRecord;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.QrCode;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Shipment;
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

    public List<CultivationEvent> findCultivationEventsByBatchId(Long batchId) {
        return em.createQuery(
                        "SELECT e FROM CultivationEvent e WHERE e.batchId = :batchId "
                                + "AND e.status IN ('CONFIRMED', 'APPROVED') "
                                + "ORDER BY e.recordedAt DESC, e.id DESC",
                        CultivationEvent.class)
                .setParameter("batchId", batchId)
                .getResultList();
    }

    public List<EventAttachment> findAttachmentsByEventIds(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) return List.of();
        return em.createQuery(
                        "SELECT a FROM EventAttachment a WHERE a.eventId IN :eventIds ORDER BY a.id",
                        EventAttachment.class)
                .setParameter("eventIds", eventIds)
                .getResultList();
    }

    public Optional<Shipment> findLatestShipmentByBatchId(Long batchId) {
        return em.createQuery(
                        "SELECT s FROM Shipment s WHERE s.batchId = :batchId "
                                + "ORDER BY s.shippedAt DESC, s.id DESC",
                        Shipment.class)
                .setParameter("batchId", batchId)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }
}
