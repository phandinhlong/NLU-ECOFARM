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
    private EntityManager entityManager;

    @Override
    public void persist(QcRecord record) {
        this.entityManager.persist(record);
    }

    @Override
    public QcRecord merge(QcRecord record) {
        return this.entityManager.merge(record);
    }

    @Override
    public List<QcRecord> findActiveByBatchIdOrderByIdDesc(Long batchId) {
        return this.entityManager.createQuery(
                        "select q from QcRecord q where q.batch.id = :batchId and q.isActive = true order by q.id desc",
                        QcRecord.class)
                .setParameter("batchId", batchId)
                .getResultList();
    }
}
