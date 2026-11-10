package jakarta.ecofarm_backend.infratructure.persistence.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

import jakarta.ecofarm_backend.infratructure.persistence.entities.OutboxEvent;

@Stateless
public class OutboxEventRepository {

    @PersistenceContext
    private EntityManager em;

    public void persist(OutboxEvent event) {
        em.persist(event);
    }

    public OutboxEvent findById(Long id) {
        return em.find(OutboxEvent.class, id);
    }

    public List<OutboxEvent> findPending() {
        TypedQuery<OutboxEvent> q = em.createQuery(
            "select e from OutboxEvent e where e.status = 'PENDING' order by e.createdAt asc",
            OutboxEvent.class);
        return q.getResultList();
    }

    public void incrementAttempts(OutboxEvent event) {
        int attempts = event.getAttempts() + 1;
        event.setAttempts(attempts);
        event.setStatus(attempts >= 5 ? OutboxEvent.OutboxStatus.FAILED : OutboxEvent.OutboxStatus.PENDING);
        if (event.getStatus() == OutboxEvent.OutboxStatus.PENDING) {
            long nextAt = System.currentTimeMillis() + (1L << attempts) * 10000;
            event.setNextAttemptAt(nextAt);
        }
        event.setUpdatedAt(System.currentTimeMillis());
        em.merge(event);
    }

    public void complete(OutboxEvent event) {
        event.setStatus(OutboxEvent.OutboxStatus.SUCCESS);
        event.setUpdatedAt(System.currentTimeMillis());
        em.merge(event);
    }

    public void markFailed(OutboxEvent event, String errorMsg) {
        event.setStatus(OutboxEvent.OutboxStatus.FAILED);
        event.setUpdatedAt(System.currentTimeMillis());
        int attempts = event.getAttempts() + 1;
        event.setAttempts(attempts);
        event.setNextAttemptAt(null);
        em.merge(event);
    }

    public boolean existsProcessedByBatchIdHashAndSeq(String batchIdHash, int seq) {
        TypedQuery<Long> q = em.createQuery(
            "select count(e) from OutboxEvent e where e.batchIdHash = :bid and e.seq = :seq and e.status = :status",
            Long.class);
        q.setParameter("bid", batchIdHash);
        q.setParameter("seq", seq);
        q.setParameter("status", OutboxEvent.OutboxStatus.SUCCESS);
        Long count = q.getSingleResult();
        return count > 0;
    }
}
