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
        return em.find(Batch.class, id);
    }

    public Batch merge(Batch batch) {
        return em.merge(batch);
    }
}
