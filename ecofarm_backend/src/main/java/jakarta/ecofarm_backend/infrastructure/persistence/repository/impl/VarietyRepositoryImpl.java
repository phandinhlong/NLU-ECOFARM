package jakarta.ecofarm_backend.infrastructure.persistence.repository.impl;

import jakarta.ecofarm_backend.common.dto.request.VarietyCriteria;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Variety;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.VarietyRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class VarietyRepositoryImpl implements VarietyRepository {

    @PersistenceContext
    private EntityManager em;

    private void appendFilters(StringBuilder jpql, VarietyCriteria criteria) {
        criteria.getKeywordFilter().ifPresent(value -> jpql.append(" AND (LOWER(v.code) LIKE :keyword OR LOWER(v.name) LIKE :keyword)"));
    }

    private void bindFilters(TypedQuery<?> query, VarietyCriteria criteria) {
        criteria.getKeywordFilter().ifPresent(value -> query.setParameter("keyword", "%" + value.trim().toLowerCase() + "%"));
    }

    @Override
    public List<Variety> findPage(VarietyCriteria criteria) {
        StringBuilder jpql = new StringBuilder("""
                SELECT v FROM Variety v
                WHERE v.isActive = true
                """);
        appendFilters(jpql, criteria);
        jpql.append(" AND (:lastId IS NULL OR v.id < :lastId) ORDER BY v.id DESC");

        TypedQuery<Variety> query = em.createQuery(jpql.toString(), Variety.class);
        bindFilters(query, criteria);
        query.setParameter("lastId", criteria.getLastIdFilter().orElse(null));
        query.setMaxResults(VarietyCriteria.LENGTH + 1);
        return query.getResultList();
    }

    @Override
    public Long countActive(VarietyCriteria criteria) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(v) FROM Variety v WHERE v.isActive = true");
        appendFilters(jpql, criteria);

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        bindFilters(query, criteria);
        return query.getSingleResult();
    }

    @Override
    public Map<Long, Long> countBatchesByVarietyIds(List<Long> varietyIds) {
        Map<Long, Long> result = new HashMap<>();
        if (varietyIds == null || varietyIds.isEmpty()) {
            return result;
        }
        List<Object[]> rows = em.createQuery("""
                SELECT b.variety.id, COUNT(b) FROM Batch b
                WHERE b.variety.id IN :varietyIds
                GROUP BY b.variety.id
                """, Object[].class)
                .setParameter("varietyIds", varietyIds)
                .getResultList();
        for (Object[] row : rows) {
            result.put((Long) row[0], (Long) row[1]);
        }
        return result;
    }

    @Override
    public Variety findById(Long id) {
        return em.find(Variety.class, id);
    }

    @Override
    public boolean existsByCode(String code) {
        return existsByCodeAndIdNot(code, null);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, Long id) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(v) FROM Variety v WHERE v.isActive = true AND v.code = :code");
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

    @Override
    public Long countBatches(Long varietyId) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(b) FROM Batch b WHERE b.variety.id = :varietyId",
                Long.class);
        query.setParameter("varietyId", varietyId);
        return query.getSingleResult();
    }

    @Override
    public Variety save(Variety variety) {
        if (variety.getId() == null) {
            em.persist(variety);
            return variety;
        }
        return em.merge(variety);
    }
}
