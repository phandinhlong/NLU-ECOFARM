package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Recall;

@Stateless
public class RecallRepository {

    @PersistenceContext
    private EntityManager em;

    public void persist(Recall recall) {
        em.persist(recall);
    }

    public Recall findById(Long id) {
        return em.find(Recall.class, id);
    }

    public List<Recall> findByBatchId(Long batchId) {
        TypedQuery<Recall> q = em.createQuery(
                "SELECT r FROM Recall r WHERE r.batch.id = :batchId ORDER BY r.createdAt DESC",
                Recall.class);
        q.setParameter("batchId", batchId);
        return q.getResultList();
    }
}
