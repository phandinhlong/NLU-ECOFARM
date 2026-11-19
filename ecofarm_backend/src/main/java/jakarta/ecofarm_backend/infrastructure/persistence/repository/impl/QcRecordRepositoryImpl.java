package jakarta.ecofarm_backend.infrastructure.persistence.repository.impl;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.QcRecord;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.QcRecordRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class QcRecordRepositoryImpl implements QcRecordRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void persist(QcRecord record) {
        em.persist(record);
    }

    @Override
    public QcRecord merge(QcRecord record) {
        return em.merge(record);
    }

    @Override
    public QcRecord findById(Long id) {
        return em.find(QcRecord.class, id);
    }

    @Override
    public List<QcRecord> findByBatchId(Long batchId) {
        return em.createQuery("select q from QcRecord q where q.batch.id = :batchId order by q.id asc", QcRecord.class)
                .setParameter("batchId", batchId)
                .getResultList();
    }

    @Override
    public QcRecord findLatestByBatchId(Long batchId) {
        List<QcRecord> list = em.createQuery("select q from QcRecord q where q.batch.id = :batchId order by q.id desc", QcRecord.class)
                .setParameter("batchId", batchId)
                .setMaxResults(1)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public int countByBatchId(Long batchId) {
        Long count = em.createQuery("select count(q) from QcRecord q where q.batch.id = :batchId", Long.class)
                .setParameter("batchId", batchId)
                .getSingleResult();
        return count == null ? 0 : count.intValue();
    }

    @Override
    public QcRecord findByClientEventId(String clientEventId) {
        List<QcRecord> list = em.createQuery("select q from QcRecord q where q.clientEventId = :clientEventId", QcRecord.class)
                .setParameter("clientEventId", clientEventId)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }
}
