package jakarta.ecofarm_backend.infrastructure.persistence.repository.impl;

import jakarta.ecofarm_backend.common.dto.request.GreenhouseCriteria;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Greenhouse;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.GreenhouseRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class GreenhouseRepositoryImpl implements GreenhouseRepository {

    @PersistenceContext
    private EntityManager em;

    private void appendFilters(StringBuilder jpql, GreenhouseCriteria criteria) {
        criteria.getStatusFilter().ifPresent(value -> jpql.append(" AND g.status = :status"));
        criteria.getKeywordFilter().ifPresent(value -> jpql.append(" AND (LOWER(g.code) LIKE :keyword OR LOWER(g.name) LIKE :keyword)"));
    }

    private void bindFilters(TypedQuery<?> query, GreenhouseCriteria criteria) {
        criteria.getStatusFilter().ifPresent(value -> query.setParameter("status", value));
        criteria.getKeywordFilter().ifPresent(value -> query.setParameter("keyword", "%" + value.trim().toLowerCase() + "%"));
    }

    @Override
    public List<Greenhouse> findPage(GreenhouseCriteria criteria) {
        StringBuilder jpql = new StringBuilder("""
                SELECT g FROM Greenhouse g
                WHERE g.isActive = true
                """);
        appendFilters(jpql, criteria);
        jpql.append(" AND (:lastId IS NULL OR g.id < :lastId) ORDER BY g.id DESC");

        TypedQuery<Greenhouse> query = em.createQuery(jpql.toString(), Greenhouse.class);
        bindFilters(query, criteria);
        query.setParameter("lastId", criteria.getLastIdFilter().orElse(null));
        query.setMaxResults(GreenhouseCriteria.LENGTH + 1);
        return query.getResultList();
    }

    @Override
    public Long countActive(GreenhouseCriteria criteria) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(g) FROM Greenhouse g WHERE g.isActive = true");
        appendFilters(jpql, criteria);

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        bindFilters(query, criteria);
        return query.getSingleResult();
    }

    @Override
    public Map<Long, Long> countBatchesByGreenhouseIds(List<Long> greenhouseIds) {
        Map<Long, Long> result = new HashMap<>();
        if (greenhouseIds == null || greenhouseIds.isEmpty()) {
            return result;
        }
        List<Object[]> rows = em.createQuery("""
                SELECT b.greenhouse.id, COUNT(b) FROM Batch b
                WHERE b.greenhouse.id IN :greenhouseIds
                GROUP BY b.greenhouse.id
                """, Object[].class)
                .setParameter("greenhouseIds", greenhouseIds)
                .getResultList();
        for (Object[] row : rows) {
            result.put((Long) row[0], (Long) row[1]);
        }
        return result;
    }

    @Override
    public Greenhouse findById(Long id) {
        return em.find(Greenhouse.class, id);
    }

    @Override
    public boolean existsByCode(String code) {
        return existsByCodeAndIdNot(code, null);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, Long id) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(g) FROM Greenhouse g WHERE g.code = :code");
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

    @Override
    public Long countBatches(Long greenhouseId) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(b) FROM Batch b WHERE b.greenhouse.id = :greenhouseId",
                Long.class);
        query.setParameter("greenhouseId", greenhouseId);
        return query.getSingleResult();
    }

    @Override
    public Long countCertificates(Long greenhouseId) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM Certificate c WHERE c.greenhouse.id = :greenhouseId",
                Long.class);
        query.setParameter("greenhouseId", greenhouseId);
        return query.getSingleResult();
    }

    @Override
    public void delete(Greenhouse greenhouse) {
        em.remove(greenhouse);
    }

    @Override
    public Greenhouse save(Greenhouse greenhouse) {
        if (greenhouse.getId() == null) {
            em.persist(greenhouse);
            return greenhouse;
        }
        return em.merge(greenhouse);
    }
}
