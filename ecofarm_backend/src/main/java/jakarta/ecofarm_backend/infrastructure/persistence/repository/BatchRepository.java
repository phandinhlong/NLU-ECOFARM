package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class BatchRepository {

    @PersistenceContext
    private EntityManager em;

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
}
