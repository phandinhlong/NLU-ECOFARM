package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class CultivationEventRepository {

    @PersistenceContext
    private EntityManager em;

    public void persist(CultivationEvent event) {
        em.persist(event);
    }

    public CultivationEvent merge(CultivationEvent event) {
        return em.merge(event);
    }

    public CultivationEvent findById(Long id) {
        return em.find(CultivationEvent.class, id);
    }

    public CultivationEvent findByClientEventId(String clientEventId) {
        List<CultivationEvent> list = em.createQuery(
                        "select e from CultivationEvent e where e.clientEventId = :clientEventId",
                        CultivationEvent.class)
                .setParameter("clientEventId", clientEventId)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public Integer findNextSeqByBatchId(Long batchId) {
        Integer maxSeq = em.createQuery(
                        "select max(e.seq) from CultivationEvent e where e.batch.id = :batchId",
                        Integer.class)
                .setParameter("batchId", batchId)
                .getSingleResult();
        return (maxSeq == null ? 0 : maxSeq) + 1;
    }

    public List<CultivationEvent> findByBatchIdOrderBySeqAsc(Long batchId) {
        return em.createQuery(
                        "select e from CultivationEvent e where e.batch.id = :batchId and (e.isActive is null or e.isActive = true) order by e.seq asc, e.id asc",
                        CultivationEvent.class)
                .setParameter("batchId", batchId)
                .getResultList();
    }
}
