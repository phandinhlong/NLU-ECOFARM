package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

@Stateless
public class CultivationEventRepository {

    @PersistenceContext
    private EntityManager em;

    public List<CultivationEvent> findByBatchIdOrderBySeqAsc(Long batchId) {
        return em.createQuery(
                        "select e from CultivationEvent e where e.batch.id = :batchId and (e.isActive is null or e.isActive = true) order by e.seq asc, e.id asc",
                        CultivationEvent.class)
                .setParameter("batchId", batchId)
                .getResultList();
    }

    /**
     * Finds active CultivationEvent by ID with batch eagerly fetched.
     *
     * @param id event ID
     * @return Optional containing entity if found and active, otherwise Optional.empty()
     */
    public Optional<CultivationEvent> findById(Long id) {
        List<CultivationEvent> results = em.createQuery(
                        "select e from CultivationEvent e left join fetch e.batch where e.id = :id and (e.isActive is null or e.isActive = true)",
                        CultivationEvent.class)
                .setParameter("id", id)
                .getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
