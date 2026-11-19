package jakarta.ecofarm_backend.infrastructure.persistence.repository.impl;

import jakarta.ecofarm_backend.common.enums.TaskStatus;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.BatchTask;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.BatchTaskRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

@Stateless
public class BatchTaskRepositoryImpl implements BatchTaskRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public BatchTask save(BatchTask task) {
        if (task.getId() == null) {
            em.persist(task);
            return task;
        }
        return em.merge(task);
    }

    @Override
    public Optional<BatchTask> findById(Long id) {
        List<BatchTask> rows = em.createQuery(
                "select distinct t from BatchTask t left join fetch t.batch left join fetch t.worker left join fetch t.assignedBy left join fetch t.event left join fetch t.completionEvent where t.id = :id",
                BatchTask.class).setParameter("id", id).getResultList();
        return rows.stream().findFirst();
    }

    @Override
    public List<BatchTask> findPageByBatchId(Long batchId, Long lastIndex, TaskStatus status, int limit) {
        return em.createQuery(
                "select distinct t from BatchTask t left join fetch t.worker left join fetch t.assignedBy where t.batch.id = :batchId and (t.isActive is null or t.isActive = true) and (:status is null or t.status = :status) and (:lastIndex is null or t.id < :lastIndex) order by t.id desc",
                BatchTask.class)
                .setParameter("batchId", batchId)
                .setParameter("status", status)
                .setParameter("lastIndex", lastIndex)
                .setMaxResults(limit)
                .getResultList();
    }
}
