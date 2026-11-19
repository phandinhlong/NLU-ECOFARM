package jakarta.ecofarm_backend.infrastructure.persistence.repository.impl;

import jakarta.ecofarm_backend.common.dto.request.PageCriteria;
import jakarta.ecofarm_backend.common.enums.EventType;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.BatchAssignment;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
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
        criteria.getStatusFilter().ifPresent(value -> jpql.append(" AND b.status = :status"));
        criteria.getGreenhouseFilter().ifPresent(value -> jpql.append(" AND b.greenhouse.id = :greenhouseId"));
        criteria.getBatchCodeKeywordFilter().ifPresent(value -> jpql.append(" AND LOWER(b.batchCode) LIKE :batchCodeKeyword"));
        criteria.getMineUserFilter().ifPresent(value ->
                jpql.append(" AND EXISTS (SELECT a FROM BatchAssignment a WHERE a.batch = b AND a.user.id = :mineUserId)"));
    }

    private void bindFilters(TypedQuery<?> query, PageCriteria criteria) {
        criteria.getStatusFilter().ifPresent(value -> query.setParameter("status", value.name()));
        criteria.getGreenhouseFilter().ifPresent(value -> query.setParameter("greenhouseId", value));
        criteria.getBatchCodeKeywordFilter()
                .ifPresent(value -> query.setParameter("batchCodeKeyword", "%" + value.trim().toLowerCase() + "%"));
        criteria.getMineUserFilter().ifPresent(value -> query.setParameter("mineUserId", value));
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
        jpql.append(" AND (:lastIndex IS NULL OR b.id < :lastIndex) ORDER BY b.id DESC");

        TypedQuery<Batch> query = em.createQuery(jpql.toString(), Batch.class);
        bindFilters(query, criteria);
        query.setParameter("lastIndex", criteria.getLastIndexFilter().orElse(null));
        query.setMaxResults(PageCriteria.LENGTH + 1);
        return query.getResultList();
    }

    @Override
    public Batch findById(Long id) {
        return em.createQuery("SELECT b FROM Batch b LEFT JOIN FETCH b.greenhouse LEFT JOIN FETCH b.variety LEFT JOIN FETCH b.createdBy WHERE b.id = :id", Batch.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Batch findByBatchCode(String batchCode) {
        try {
            return em.createQuery("SELECT b FROM Batch b LEFT JOIN FETCH b.greenhouse LEFT JOIN FETCH b.variety WHERE b.batchCode = :code", Batch.class)
                    .setParameter("code", batchCode)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Batch save(Batch batch) {
        if (batch.getId() == null) {
            em.persist(batch);
            return batch;
        }
        return em.merge(batch);
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
                  AND (e.status IS NULL OR e.status IN ('CONFIRMED', 'SUPERSEDED'))
                  AND (e.isActive IS NULL OR e.isActive = true)
                """, Long.class);
        query.setParameter("batchId", batchId);
        query.setParameter("eventType", EventType.POLLINATION);
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
                  AND (e.status IS NULL OR e.status IN ('CONFIRMED', 'SUPERSEDED'))
                  AND (e.isActive IS NULL OR e.isActive = true)
                GROUP BY e.batch.id
                """, Object[].class);
        query.setParameter("batchIds", batchIds);
        query.setParameter("eventType", EventType.POLLINATION);
        for (Object[] row : query.getResultList()) {
            result.put((Long) row[0], (Long) row[1]);
        }
        return result;
    }

    @Override
    public List<BatchAssignment> findAssignments(Long batchId) {
        return em.createQuery("""
                SELECT DISTINCT a FROM BatchAssignment a
                JOIN FETCH a.assignee
                LEFT JOIN FETCH a.assignee.roles
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
                WHERE a.batch.id = :batchId AND a.assignee.id = :userId
                  AND (a.isActive IS NULL OR a.isActive = true)
                """, Long.class);
        query.setParameter("batchId", batchId);
        query.setParameter("userId", userId);
        return query.getSingleResult() > 0;
    }

    @Override
    public List<User> findAssignedWorkerPage(Long batchId, Long lastIndex, int limit) {
        TypedQuery<User> query = em.createQuery("""
                SELECT DISTINCT a.assignee FROM BatchAssignment a JOIN a.assignee u
                LEFT JOIN FETCH u.roles
                WHERE a.batch.id = :batchId AND (a.isActive IS NULL OR a.isActive = true)
                  AND (:lastIndex IS NULL OR u.id < :lastIndex)
                ORDER BY u.id DESC
                """, User.class);
        query.setParameter("batchId", batchId);
        query.setParameter("lastIndex", lastIndex);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public List<Long> findAssignedUserIds(Long batchId) {
        return em.createQuery("""
                SELECT a.assignee.id FROM BatchAssignment a
                WHERE a.batch.id = :batchId AND (a.isActive IS NULL OR a.isActive = true)
                """, Long.class)
                .setParameter("batchId", batchId)
                .getResultList();
    }

    @Override
    public void removeAssignments(Long batchId, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        findAssignments(batchId).stream()
                .filter(a -> userIds.contains(a.getUser().getId()))
                .forEach(a -> a.setActive(false));
    }

    @Override
    public void persistAssignment(BatchAssignment assignment) {
        em.persist(assignment);
    }

    @Override
    public Boolean isOwnerBatch(Long batchId, Long userId) {
        TypedQuery<Long> query = em.createQuery("""
                SELECT COUNT(a) FROM BatchAssignment a
                WHERE a.batch.id = :batchId AND a.assigner.id = :userId
                  AND (a.isActive IS NULL OR a.isActive = true)
                """, Long.class);
        query.setParameter("batchId", batchId);
        query.setParameter("userId", userId);
        return query.getSingleResult() > 0;
    }
}
