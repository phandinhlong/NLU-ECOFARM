package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.BatchAssignment;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class BatchRepository {

    @PersistenceContext
    private EntityManager em;

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

    private void appendFilters(StringBuilder jpql, String status, Long greenhouseId, String q, Long mineUserId) {
        if (status != null && !status.isBlank()) {
            jpql.append(" AND b.status = :status");
        }
        if (greenhouseId != null) {
            jpql.append(" AND b.greenhouse.id = :greenhouseId");
        }
        if (q != null && !q.isBlank()) {
            jpql.append(" AND LOWER(b.batchCode) LIKE :q");
        }
        if (mineUserId != null) {
            jpql.append(" AND EXISTS (SELECT a FROM BatchAssignment a WHERE a.batch = b AND a.user.id = :mineUserId)");
        }
    }

    private void bindFilters(TypedQuery<?> query, String status, Long greenhouseId, String q, Long mineUserId) {
        if (status != null && !status.isBlank()) {
            query.setParameter("status", status);
        }
        if (greenhouseId != null) {
            query.setParameter("greenhouseId", greenhouseId);
        }
        if (q != null && !q.isBlank()) {
            query.setParameter("q", "%" + q.trim().toLowerCase() + "%");
        }
        if (mineUserId != null) {
            query.setParameter("mineUserId", mineUserId);
        }
    }

    public List<Batch> findPage(int page, int size, String status, Long greenhouseId, String q, Long mineUserId) {
        StringBuilder jpql = new StringBuilder("""
                SELECT b FROM Batch b
                LEFT JOIN FETCH b.greenhouse
                LEFT JOIN FETCH b.variety
                WHERE (b.isActive IS NULL OR b.isActive = true)
                """);
        appendFilters(jpql, status, greenhouseId, q, mineUserId);
        jpql.append(" ORDER BY b.id DESC");

        TypedQuery<Batch> query = em.createQuery(jpql.toString(), Batch.class);
        bindFilters(query, status, greenhouseId, q, mineUserId);
        query.setFirstResult(safeOffset(page, size));
        query.setMaxResults(safeSize(size));
        return query.getResultList();
    }

    public long countActive(String status, Long greenhouseId, String q, Long mineUserId) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(b) FROM Batch b WHERE (b.isActive IS NULL OR b.isActive = true)");
        appendFilters(jpql, status, greenhouseId, q, mineUserId);

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        bindFilters(query, status, greenhouseId, q, mineUserId);
        return query.getSingleResult();
    }

    public Batch findById(Long id) {
        try {
            return em.createQuery("SELECT b FROM Batch b LEFT JOIN FETCH b.greenhouse LEFT JOIN FETCH b.variety WHERE b.id = :id", Batch.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (Exception e) {
            return em.find(Batch.class, id);
        }
    }

    public Batch findByBatchCode(String batchCode) {
        try {
            return em.createQuery("SELECT b FROM Batch b LEFT JOIN FETCH b.greenhouse LEFT JOIN FETCH b.variety WHERE b.batchCode = :code", Batch.class)
                    .setParameter("code", batchCode)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public Batch merge(Batch batch) {
        return em.merge(batch);
    }

    public void persist(Batch batch) {
        em.persist(batch);
    }

    public boolean existsByBatchCode(String batchCode) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(b) FROM Batch b WHERE b.batchCode = :batchCode", Long.class);
        query.setParameter("batchCode", batchCode);
        return query.getSingleResult() > 0;
    }

    public long countEvents(Long batchId) {
        TypedQuery<Long> query = em.createQuery("""
                SELECT COUNT(e) FROM CultivationEvent e
                WHERE e.batch.id = :batchId AND (e.isActive IS NULL OR e.isActive = true)
                """, Long.class);
        query.setParameter("batchId", batchId);
        return query.getSingleResult();
    }

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

    public boolean isAssigned(Long batchId, Long userId) {
        TypedQuery<Long> query = em.createQuery("""
                SELECT COUNT(a) FROM BatchAssignment a
                WHERE a.batch.id = :batchId AND a.user.id = :userId
                  AND (a.isActive IS NULL OR a.isActive = true)
                """, Long.class);
        query.setParameter("batchId", batchId);
        query.setParameter("userId", userId);
        return query.getSingleResult() > 0;
    }

    public void deleteAssignmentsByBatch(Long batchId) {
        em.createQuery("DELETE FROM BatchAssignment a WHERE a.batch.id = :batchId")
                .setParameter("batchId", batchId)
                .executeUpdate();
    }

    public void persistAssignment(BatchAssignment assignment) {
        em.persist(assignment);
    }
}
