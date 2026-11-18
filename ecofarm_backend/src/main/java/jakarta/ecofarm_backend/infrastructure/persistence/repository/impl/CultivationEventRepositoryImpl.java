package jakarta.ecofarm_backend.infrastructure.persistence.repository.impl;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.CultivationEventRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

@Stateless
public class CultivationEventRepositoryImpl implements CultivationEventRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<CultivationEvent> findByBatchIdOrderBySeqAsc(Long batchId) {
        return em.createQuery(
                        "select e from CultivationEvent e where e.batch.id = :batchId and (e.isActive is null or e.isActive = true) order by e.seq asc, e.id asc",
                        CultivationEvent.class)
                .setParameter("batchId", batchId)
                .getResultList();
    }

    @Override
    public CultivationEvent save(CultivationEvent event) {
        if (event.getId() == null) {
            em.persist(event);
            return event;
        }
        return em.merge(event);
    }

    @Override
    public Optional<CultivationEvent> findById(Long id) {
        List<CultivationEvent> rows = em.createQuery(
                "select distinct e from CultivationEvent e left join fetch e.batch left join fetch e.recordedBy left join fetch e.eventAttachments where e.id = :id",
                CultivationEvent.class).setParameter("id", id).getResultList();
        return rows.stream().findFirst();
    }

    @Override
    public Optional<CultivationEvent> findByClientEventId(String clientEventId) {
        List<CultivationEvent> rows = em.createQuery(
                "select distinct e from CultivationEvent e left join fetch e.batch left join fetch e.recordedBy left join fetch e.eventAttachments where e.clientEventId = :clientEventId",
                CultivationEvent.class).setParameter("clientEventId", clientEventId).getResultList();
        return rows.stream().findFirst();
    }
}
