package jakarta.ecofarm_backend.infrastructure.persistence.repository.impl;

import jakarta.ecofarm_backend.common.dto.request.PageCriteria;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.BatchAssignment;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.BatchRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class BatchRepositoryImpl implements BatchRepository {

    @PersistenceContext
    private EntityManager em;

    private void appendFilters(StringBuilder jpql, PageCriteria criteria) {
        if (criteria.getStatus() != null && !criteria.getStatus().isBlank()) {
            jpql.append(" AND b.status = :status");
        }
        if (criteria.getGreenhouseId() != null) {
            jpql.append(" AND b.greenhouse.id = :greenhouseId");
        }
        if (criteria.getQ() != null && !criteria.getQ().isBlank()) {
            jpql.append(" AND LOWER(b.batchCode) LIKE :q");
        }
        if (criteria.getMineUserId() != null) {
            jpql.append(" AND EXISTS (SELECT a FROM BatchAssignment a WHERE a.batch = b AND a.user.id = :mineUserId)");
        }
    }

    private void bindFilters(TypedQuery<?> query, PageCriteria criteria) {
        if (criteria.getStatus() != null && !criteria.getStatus().isBlank()) {
            query.setParameter("status", criteria.getStatus());
        }
        if (criteria.getGreenhouseId() != null) {
            query.setParameter("greenhouseId", criteria.getGreenhouseId());
        }
        if (criteria.getQ() != null && !criteria.getQ().isBlank()) {
            query.setParameter("q", "%" + criteria.getQ().trim().toLowerCase() + "%");
        }
        if (criteria.getMineUserId() != null) {
            query.setParameter("mineUserId", criteria.getMineUserId());
        }
    }

    @Override
    public List<Batch> findPage(PageCriteria criteria) {
        StringBuilder jpql = new StringBuilder("""
                SELECT b FROM Batch b
                LEFT JOIN FETCH b.greenhouse
                LEFT JOIN FETCH b.variety
                WHERE (b.isActive IS NULL OR b.isActive = true)
                """);
        appendFilters(jpql, criteria);
        jpql.append(" ORDER BY b.id DESC");

        TypedQuery<Batch> query = em.createQuery(jpql.toString(), Batch.class);
        bindFilters(query, criteria);
        query.setFirstResult(criteria.getSafeOffset());
        query.setMaxResults(criteria.getSafeSize());
        return query.getResultList();
    }

    @Override
    public Long countActive(PageCriteria criteria) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(b) FROM Batch b WHERE (b.isActive IS NULL OR b.isActive = true)");
        appendFilters(jpql, criteria);

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        bindFilters(query, criteria);
        return query.getSingleResult();
    }

    @Override
    public Batch findById(Long id) {
        return em.find(Batch.class, id);
    }

    @Override
    public Batch merge(Batch batch) {
        return em.merge(batch);
    }

    @Override
    public void persist(Batch batch) {
        em.persist(batch);
    }

    @Override
    public Boolean existsByBatchCode(String batchCode) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(b) FROM Batch b WHERE b.batchCode = :batchCode", Long.class);
        query.setParameter("batchCode", batchCode);
        return query.getSingleResult() > 0;
    }

    @Override
    public Long countEvents(Long batchId) {
        TypedQuery<Long> query = em.createQuery("""
                SELECT COUNT(e) FROM CultivationEvent e
                WHERE e.batch.id = :batchId AND (e.isActive IS NULL OR e.isActive = true)
                """, Long.class);
        query.setParameter("batchId", batchId);
        return query.getSingleResult();
    }

    @Override
    public Long findPollinationDate(Long batchId) {
        TypedQuery<Long> query = em.createQuery("""
                SELECT MIN(e.recordedAt) FROM CultivationEvent e
                WHERE e.batch.id = :batchId AND e.eventType = :eventType
                  AND (e.status IS NULL OR e.status IN ('APPROVED', 'SUPERSEDED'))
                  AND (e.isActive IS NULL OR e.isActive = true)
                """, Long.class);
        query.setParameter("batchId", batchId);
        query.setParameter("eventType", "THU_PHAN");
        return query.getSingleResult();
    }

    @Override
    public Map<Long, Long> findPollinationDates(List<Long> batchIds) {
        Map<Long, Long> result = new HashMap<>();
        if (batchIds == null || batchIds.isEmpty()) {
            return result;
        }
        TypedQuery<Object[]> query = em.createQuery("""
                SELECT e.batch.id, MIN(e.recordedAt) FROM CultivationEvent e
                WHERE e.batch.id IN :batchIds AND e.eventType = :eventType
                  AND (e.status IS NULL OR e.status IN ('APPROVED', 'SUPERSEDED'))
                  AND (e.isActive IS NULL OR e.isActive = true)
                GROUP BY e.batch.id
                """, Object[].class);
        query.setParameter("batchIds", batchIds);
        query.setParameter("eventType", "THU_PHAN");
        for (Object[] row : query.getResultList()) {
            result.put((Long) row[0], (Long) row[1]);
        }
        return result;
    }

    @Override
    public List<BatchAssignment> findAssignments(Long batchId) {
        return em.createQuery("""
                SELECT a FROM BatchAssignment a
                JOIN FETCH a.user
                WHERE a.batch.id = :batchId AND (a.isActive IS NULL OR a.isActive = true)
                ORDER BY a.id
                """, BatchAssignment.class)
                .setParameter("batchId", batchId)
                .getResultList();
    }

    @Override
    public Map<Long, Long> countAssignmentsByBatchIds(List<Long> batchIds) {
        Map<Long, Long> result = new HashMap<>();
        if (batchIds == null || batchIds.isEmpty()) {
            return result;
        }
        List<Object[]> rows = em.createQuery("""
                SELECT a.batch.id, COUNT(a) FROM BatchAssignment a
                WHERE a.batch.id IN :batchIds AND (a.isActive IS NULL OR a.isActive = true)
                GROUP BY a.batch.id
                """, Object[].class)
                .setParameter("batchIds", batchIds)
                .getResultList();
        for (Object[] row : rows) {
            result.put((Long) row[0], (Long) row[1]);
        }
        return result;
    }

    @Override
    public Boolean isAssigned(Long batchId, Long userId) {
        TypedQuery<Long> query = em.createQuery("""
                SELECT COUNT(a) FROM BatchAssignment a
                WHERE a.batch.id = :batchId AND a.user.id = :userId
                  AND (a.isActive IS NULL OR a.isActive = true)
                """, Long.class);
        query.setParameter("batchId", batchId);
        query.setParameter("userId", userId);
        return query.getSingleResult() > 0;
    }

    @Override
    public void deleteAssignmentsByBatch(Long batchId) {
        em.createQuery("DELETE FROM BatchAssignment a WHERE a.batch.id = :batchId")
                .setParameter("batchId", batchId)
                .executeUpdate();
    }

    @Override
    public void persistAssignment(BatchAssignment assignment) {
        em.persist(assignment);
    }
}
