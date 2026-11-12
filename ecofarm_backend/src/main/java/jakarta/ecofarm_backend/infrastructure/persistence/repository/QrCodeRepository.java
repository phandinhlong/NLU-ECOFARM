package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.QrCode;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Stateless
public class QrCodeRepository {

    @PersistenceContext
    private EntityManager em;

    public void persist(QrCode qrCode) {
        em.persist(qrCode);
    }

    public QrCode merge(QrCode qrCode) {
        return em.merge(qrCode);
    }

    public QrCode findByCode(String code) {
        List<QrCode> results = em.createQuery(
                        "select q from QrCode q where q.code = :code",
                        QrCode.class)
                .setParameter("code", code)
                .setMaxResults(1)
                .getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public boolean existsByCode(String code) {
        Long cnt = em.createQuery("select count(q) from QrCode q where q.code = :code", Long.class)
                .setParameter("code", code)
                .getSingleResult();
        return cnt != null && cnt > 0;
    }

    public List<QrCode> findByPackageIdOrderBySerialAsc(Long packageId, Integer fromSerial, Integer toSerial) {
        StringBuilder jpql = new StringBuilder("select q from QrCode q where q.packageId = :packageId");
        if (fromSerial != null) jpql.append(" and q.serial >= :fromSerial");
        if (toSerial != null) jpql.append(" and q.serial <= :toSerial");
        jpql.append(" order by q.serial asc");

        TypedQuery<QrCode> query = em.createQuery(jpql.toString(), QrCode.class)
                .setParameter("packageId", packageId);
        if (fromSerial != null) query.setParameter("fromSerial", fromSerial);
        if (toSerial != null) query.setParameter("toSerial", toSerial);
        return query.getResultList();
    }

    public int countByPackageId(Long packageId) {
        Long cnt = em.createQuery("select count(q) from QrCode q where q.packageId = :packageId", Long.class)
                .setParameter("packageId", packageId)
                .getSingleResult();
        return cnt != null ? cnt.intValue() : 0;
    }

    public int findMaxSerialByPackageId(Long packageId) {
        Integer max = em.createQuery("select max(q.serial) from QrCode q where q.packageId = :packageId", Integer.class)
                .setParameter("packageId", packageId)
                .getSingleResult();
        return max != null ? max : 0;
    }
}
