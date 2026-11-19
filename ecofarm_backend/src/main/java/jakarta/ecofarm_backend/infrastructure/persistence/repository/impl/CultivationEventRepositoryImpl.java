package jakarta.ecofarm_backend.infrastructure.persistence.repository.impl;

import jakarta.ecofarm_backend.common.dto.request.EventPageCriteria;
import jakarta.ecofarm_backend.common.enums.EventReviewStatus;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.CultivationEventRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

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
    public List<CultivationEvent> findPage(EventPageCriteria criteria) {
        Long batchId = criteria.getBatchId();
        Long lastIndex = criteria.getLastIndex();
        EventReviewStatus reviewStatus = criteria.getReviewStatus();
        StringBuilder jpql = new StringBuilder(
                "select distinct e from CultivationEvent e left join fetch e.recordedBy"
                        + " where e.batch.id = :batchId and (e.isActive is null or e.isActive = true)");
        if (lastIndex != null) {
            jpql.append(" and e.id < :lastIndex");
        }
        if (reviewStatus != null) {
            jpql.append(" and e.reviewStatus = :reviewStatus");
        }
        jpql.append(" order by e.id desc");
        Query query = em.createQuery(jpql.toString(), CultivationEvent.class)
                .setParameter("batchId", batchId)
                .setMaxResults(criteria.getLimit());
        if (lastIndex != null) {
            query.setParameter("lastIndex", lastIndex);
        }
        if (reviewStatus != null) {
            query.setParameter("reviewStatus", reviewStatus);
        }
        return query.getResultList();
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
                "select distinct e from CultivationEvent e left join fetch e.batch left join fetch e.recordedBy where e.id = :id",
                CultivationEvent.class).setParameter("id", id).getResultList();
        return rows.stream().findFirst();
    }

    @Override
    public Optional<CultivationEvent> findByClientEventId(String clientEventId) {
        List<CultivationEvent> rows = em.createQuery(
                "select distinct e from CultivationEvent e left join fetch e.batch left join fetch e.recordedBy where e.clientEventId = :clientEventId",
                CultivationEvent.class).setParameter("clientEventId", clientEventId).getResultList();
        return rows.stream().findFirst();
    }
}
