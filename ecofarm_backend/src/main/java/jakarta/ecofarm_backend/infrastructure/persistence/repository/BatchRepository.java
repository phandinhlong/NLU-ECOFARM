package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Optional;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;

@Stateless
public class BatchRepository {

    @PersistenceContext
    private EntityManager em;

    public Optional<Batch> findById(Long id) {
        return Optional.ofNullable(em.find(Batch.class, id));
    }
}
