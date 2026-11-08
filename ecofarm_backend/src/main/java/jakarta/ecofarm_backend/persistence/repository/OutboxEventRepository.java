package jakarta.ecofarm_backend.persistence.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.time.Instant;
import java.util.List;
import jakarta.ecofarm_backend.persistence.entities.OutboxEvent;

@Stateless
public class OutboxEventRepository {

    @PersistenceContext
    private EntityManager em;

    public void persist(OutboxEvent event) {
        em.merge(event);
    }

    public OutboxEvent findById(Long id) {
        return em.find(OutboxEvent.class, id);
    }

    public List<OutboxEvent> findPending() {
        TypedQuery<OutboxEvent> q = em.createQuery(
            "select e from OutboxEvent e where e.status = 'PENDING' order by e.createdAt asc",
            OutboxEvent.class
        );
        return q.getResultList();
    }

    public void incrementAttempts(OutboxEvent event) {
        event.setAttempts(event.getAttempts() + 1);
        if (event.getAttempts() >= 5) {
            event.setStatus(OutboxEvent.OutboxStatus.FAILED);
        } else {
            event.setStatus(OutboxEvent.OutboxStatus.PENDING);
            event.setNextAttemptAt(Instant.now().plusSeconds((1 << event.getAttempts()) * 10));
        }
        em.merge(event);
    }

    public void complete(OutboxEvent event) {
        event.setStatus(OutboxEvent.OutboxStatus.SUCCESS);
        event.setUpdatedAt(Instant.now());
        em.merge(event);
    }

    public void markFailed(OutboxEvent event, String errorMsg) {
        event.setStatus(OutboxEvent.OutboxStatus.FAILED);
        event.setUpdatedAt(Instant.now());
        event.setAttempts(event.getAttempts() + 1);
        em.merge(event);
    }

    public boolean existsProcessedByBatchIdHashAndSeq(String batchIdHash, int seq) {
        TypedQuery<Long> q = em.createQuery(
            "select count(e) from OutboxEvent e where e.batchIdHash = :bid and e.seq = :seq and e.status = :status",
            Long.class);
        q.setParameter("bid", batchIdHash);
        q.setParameter("seq", seq);
        q.setParameter("status", OutboxEvent.OutboxStatus.SUCCESS.name());
        Long count = q.getSingleResult();
        return count > 0;
    }
}
