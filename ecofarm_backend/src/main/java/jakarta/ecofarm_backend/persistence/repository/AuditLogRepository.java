package jakarta.ecofarm_backend.persistence.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.time.Instant;
import java.util.List;
import jakarta.ecofarm_backend.persistence.entities.AuditLog;

@Stateless
public class AuditLogRepository {

    @PersistenceContext
    private EntityManager em;

    public void persist(AuditLog log) {
        em.merge(log);
    }

    public AuditLog findById(Long id) {
        return em.find(AuditLog.class, id);
    }

    public List<AuditLog> findByEntity(String entity, Long entityId, int limit) {
        TypedQuery<AuditLog> q = em.createQuery(
            "select l from AuditLog l where l.entity = :entity and l.entityId = :id order by l.createdAt desc",
            AuditLog.class
        );
        q.setParameter("entity", entity);
        q.setParameter("id", entityId);
        q.setMaxResults(limit);
        return q.getResultList();
    }

    public List<AuditLog> findByDateRange(Instant from, Instant to, int limit) {
        TypedQuery<AuditLog> q = em.createQuery(
            "select l from AuditLog l where l.createdAt between :from and :to order by l.createdAt desc",
            AuditLog.class
        );
        q.setParameter("from", from);
        q.setParameter("to", to);
        q.setMaxResults(limit);
        return q.getResultList();
    }
}
