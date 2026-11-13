package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Certificate;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;

@Stateless
public class CertificateRepository {

    @PersistenceContext
    private EntityManager em;

    private static int safeSize(int size) {
        return size <= 0 ? 20 : Math.min(size, 100);
    }

    private static int safeOffset(int page, int size) {
        long offset = (long) Math.max(page, 0) * safeSize(size);
        return offset > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) offset;
    }

    /**
     * Số ngày cảnh báo hết hạn phải là số dương; số âm hoặc 0 được coi như không lọc,
     * nên khoảng fromDate..toDate luôn hợp lệ.
     */
    private static boolean hasExpiringFilter(Integer expiringInDays) {
        return expiringInDays != null && expiringInDays > 0;
    }

    public List<Certificate> findPage(int page, int size, Long greenhouseId, Integer expiringInDays) {
        StringBuilder jpql = new StringBuilder("""
                SELECT c FROM Certificate c
                WHERE c.isActive = true
                """);
        if (greenhouseId != null) {
            jpql.append(" AND c.greenhouseId = :greenhouseId");
        }
        if (hasExpiringFilter(expiringInDays)) {
            jpql.append(" AND c.validTo >= :fromDate AND c.validTo <= :toDate");
        }
        jpql.append(" ORDER BY c.validTo ASC, c.id DESC");

        TypedQuery<Certificate> query = em.createQuery(jpql.toString(), Certificate.class);
        if (greenhouseId != null) {
            query.setParameter("greenhouseId", greenhouseId);
        }
        if (hasExpiringFilter(expiringInDays)) {
            LocalDate fromDate = LocalDate.now();
            query.setParameter("fromDate", fromDate);
            query.setParameter("toDate", fromDate.plusDays(expiringInDays));
        }
        query.setFirstResult(safeOffset(page, size));
        query.setMaxResults(safeSize(size));
        return query.getResultList();
    }

    public long countActive(Long greenhouseId, Integer expiringInDays) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(c) FROM Certificate c WHERE c.isActive = true");
        if (greenhouseId != null) {
            jpql.append(" AND c.greenhouseId = :greenhouseId");
        }
        if (hasExpiringFilter(expiringInDays)) {
            jpql.append(" AND c.validTo >= :fromDate AND c.validTo <= :toDate");
        }

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        if (greenhouseId != null) {
            query.setParameter("greenhouseId", greenhouseId);
        }
        if (hasExpiringFilter(expiringInDays)) {
            LocalDate fromDate = LocalDate.now();
            query.setParameter("fromDate", fromDate);
            query.setParameter("toDate", fromDate.plusDays(expiringInDays));
        }
        return query.getSingleResult();
    }

    public boolean existsByCertNo(String certNo) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM Certificate c WHERE c.isActive = true AND c.certNo = :certNo",
                Long.class);
        query.setParameter("certNo", certNo);
        return query.getSingleResult() > 0;
    }

    public boolean existsGreenhouse(Long greenhouseId) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(g) FROM Greenhouse g WHERE g.id = :id AND g.isActive = true",
                Long.class);
        query.setParameter("id", greenhouseId);
        return query.getSingleResult() > 0;
    }

    public void persist(Certificate certificate) {
        em.persist(certificate);
    }
}
