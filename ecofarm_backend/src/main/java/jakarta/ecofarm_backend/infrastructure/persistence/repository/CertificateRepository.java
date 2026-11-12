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

    public List<Certificate> findPage(int page, int size, Long greenhouseId, Integer expiringInDays) {
        StringBuilder jpql = new StringBuilder("""
                SELECT c FROM Certificate c
                WHERE c.active = true
                """);
        if (greenhouseId != null) {
            jpql.append(" AND c.greenhouseId = :greenhouseId");
        }
        if (expiringInDays != null && expiringInDays >= 0) {
            jpql.append(" AND c.validTo >= :fromDate AND c.validTo <= :toDate");
        }
        jpql.append(" ORDER BY c.validTo ASC, c.id DESC");

        TypedQuery<Certificate> query = em.createQuery(jpql.toString(), Certificate.class);
        if (greenhouseId != null) {
            query.setParameter("greenhouseId", greenhouseId);
        }
        if (expiringInDays != null && expiringInDays >= 0) {
            query.setParameter("fromDate", LocalDate.now());
            query.setParameter("toDate", LocalDate.now().plusDays(expiringInDays));
        }
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }

    public long countActive(Long greenhouseId, Integer expiringInDays) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(c) FROM Certificate c WHERE c.active = true");
        if (greenhouseId != null) {
            jpql.append(" AND c.greenhouseId = :greenhouseId");
        }
        if (expiringInDays != null && expiringInDays >= 0) {
            jpql.append(" AND c.validTo >= :fromDate AND c.validTo <= :toDate");
        }

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        if (greenhouseId != null) {
            query.setParameter("greenhouseId", greenhouseId);
        }
        if (expiringInDays != null && expiringInDays >= 0) {
            query.setParameter("fromDate", LocalDate.now());
            query.setParameter("toDate", LocalDate.now().plusDays(expiringInDays));
        }
        return query.getSingleResult();
    }

    public boolean existsByCertNo(String certNo) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM Certificate c WHERE c.active = true AND c.certNo = :certNo",
                Long.class);
        query.setParameter("certNo", certNo);
        return query.getSingleResult() > 0;
    }

    public boolean existsGreenhouse(Long greenhouseId) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(g) FROM Greenhouse g WHERE g.id = :id AND g.active = true",
                Long.class);
        query.setParameter("id", greenhouseId);
        return query.getSingleResult() > 0;
    }

    public void persist(Certificate certificate) {
        em.persist(certificate);
    }
}
