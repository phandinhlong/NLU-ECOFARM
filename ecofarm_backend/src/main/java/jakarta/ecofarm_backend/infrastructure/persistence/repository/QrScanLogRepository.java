package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.QrScanLog;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class QrScanLogRepository {

    @PersistenceContext
    private EntityManager em;

    public void persist(QrScanLog log) {
        em.persist(log);
    }

    public long countDistinctIpSince(Long qrCodeId, long sinceEpochMillis) {
        Long cnt = em.createQuery(
                        "select count(distinct s.ipAddr) from QrScanLog s where s.qrCode.id = :qrCodeId and s.scannedAt >= :since and s.ipAddr is not null",
                        Long.class)
                .setParameter("qrCodeId", qrCodeId)
                .setParameter("since", sinceEpochMillis)
                .getSingleResult();
        return cnt != null ? cnt : 0L;
    }
}
