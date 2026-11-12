package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ejb.Stateless;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Variety;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Stateless
public class VarietyRepository {

    @PersistenceContext
    private EntityManager em;

    public List<Variety> findPage(int page, int size, String q) {
        StringBuilder jpql = new StringBuilder("""
                SELECT v, (SELECT COUNT(b) FROM Batch b WHERE b.varietyId = v.id AND b.active = true)
                FROM Variety v
                WHERE v.active = true
                """);
        if (q != null && !q.isBlank()) {
            jpql.append(" AND (LOWER(v.code) LIKE :q OR LOWER(v.name) LIKE :q)");
        }
        jpql.append(" ORDER BY v.id DESC");

        TypedQuery<Object[]> query = em.createQuery(jpql.toString(), Object[].class);
        if (q != null && !q.isBlank()) {
            query.setParameter("q", "%" + q.trim().toLowerCase() + "%");
        }
        query.setFirstResult(page * size);
        query.setMaxResults(size);

        return query.getResultList().stream().map(row -> {
            Variety v = (Variety) row[0];
            v.setBatchCount((Long) row[1]);
            return v;
        }).toList();
    }

    public long countActive(String q) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(v) FROM Variety v WHERE v.active = true");
        if (q != null && !q.isBlank()) {
            jpql.append(" AND (LOWER(v.code) LIKE :q OR LOWER(v.name) LIKE :q)");
        }
        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        if (q != null && !q.isBlank()) {
            query.setParameter("q", "%" + q.trim().toLowerCase() + "%");
        }
        return query.getSingleResult();
    }

    public Variety findById(Long id) {
        Variety v = em.find(Variety.class, id);
        if (v != null) {
            v.setBatchCount(countBatches(id));
        }
        return v;
    }

    public boolean existsByCode(String code) {
        return existsByCodeAndIdNot(code, null);
    }

    public boolean existsByCodeAndIdNot(String code, Long id) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(v) FROM Variety v WHERE v.active = true AND v.code = :code");
        if (id != null) {
            jpql.append(" AND v.id <> :id");
        }
        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        query.setParameter("code", code);
        if (id != null) {
            query.setParameter("id", id);
        }
        return query.getSingleResult() > 0;
    }

    public long countBatches(Long varietyId) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(b) FROM Batch b WHERE b.varietyId = :varietyId AND b.active = true",
                Long.class);
        query.setParameter("varietyId", varietyId);
        return query.getSingleResult();
    }

    public void persist(Variety variety) {
        em.persist(variety);
    }

    public Variety merge(Variety variety) {
        return em.merge(variety);
    }
}
