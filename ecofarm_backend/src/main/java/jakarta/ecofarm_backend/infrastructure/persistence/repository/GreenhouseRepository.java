package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ejb.Stateless;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Greenhouse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Stateless
public class GreenhouseRepository {

    @PersistenceContext
    private EntityManager em;

    private void appendFilters(StringBuilder jpql, String status, String q) {
        if (status != null && !status.isBlank()) {
            jpql.append(" AND g.status = :status");
        }
        if (q != null && !q.isBlank()) {
            jpql.append(" AND (LOWER(g.code) LIKE :q OR LOWER(g.name) LIKE :q)");
        }
    }

    private void bindFilters(TypedQuery<?> query, String status, String q) {
        if (status != null && !status.isBlank()) {
            query.setParameter("status", status);
        }
        if (q != null && !q.isBlank()) {
            query.setParameter("q", "%" + q.trim().toLowerCase() + "%");
        }
    }

    private static int safePage(int page) {
        return Math.max(page, 0);
    }

    private static int safeSize(int size) {
        return size <= 0 ? 20 : Math.min(size, 100);
    }

    private static int safeOffset(int page, int size) {
        long offset = (long) safePage(page) * safeSize(size);
        return offset > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) offset;
    }

    public List<Greenhouse> findPage(int page, int size, String status, String q) {
        StringBuilder jpql = new StringBuilder("""
                SELECT g, (SELECT COUNT(b) FROM Batch b WHERE b.greenhouseId = g.id)
                FROM Greenhouse g
                WHERE g.active = true
                """);
        appendFilters(jpql, status, q);
        jpql.append(" ORDER BY g.id DESC");

        TypedQuery<Object[]> query = em.createQuery(jpql.toString(), Object[].class);
        bindFilters(query, status, q);
        query.setFirstResult(safeOffset(page, size));
        query.setMaxResults(safeSize(size));

        return query.getResultList().stream().map(row -> {
            Greenhouse g = (Greenhouse) row[0];
            g.setBatchCount((Long) row[1]);
            return g;
        }).toList();
    }

    public long countActive(String status, String q) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(g) FROM Greenhouse g WHERE g.active = true");
        appendFilters(jpql, status, q);

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        bindFilters(query, status, q);
        return query.getSingleResult();
    }

    public Greenhouse findById(Long id) {
        Greenhouse g = em.find(Greenhouse.class, id);
        if (g != null) {
            g.setBatchCount(countBatches(id));
        }
        return g;
    }

    public boolean existsByCode(String code) {
        return existsByCodeAndIdNot(code, null);
    }

    public boolean existsByCodeAndIdNot(String code, Long id) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(g) FROM Greenhouse g WHERE g.active = true AND g.code = :code");
        if (id != null) {
            jpql.append(" AND g.id <> :id");
        }
        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        query.setParameter("code", code);
        if (id != null) {
            query.setParameter("id", id);
        }
        return query.getSingleResult() > 0;
    }

    public long countBatches(Long greenhouseId) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(b) FROM Batch b WHERE b.greenhouseId = :greenhouseId",
                Long.class);
        query.setParameter("greenhouseId", greenhouseId);
        return query.getSingleResult();
    }

    public void persist(Greenhouse greenhouse) {
        em.persist(greenhouse);
    }

    public Greenhouse merge(Greenhouse greenhouse) {
        return em.merge(greenhouse);
    }
}
