package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.QcRecord;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.QcRecordRepository;
import jakarta.ecofarm_backend.infrastructure.service.QcRecordService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class QcRecordServiceImpl implements QcRecordService {

    @Inject
    private QcRecordRepository qcRecordRepository;

    @Override
    public void persist(QcRecord record) {
        qcRecordRepository.persist(record);
    }

    @Override
    public QcRecord merge(QcRecord record) {
        return qcRecordRepository.merge(record);
    }

    @Override
    public QcRecord findById(Long id) {
        return qcRecordRepository.findById(id);
    }

    @Override
    public QcRecord findByClientEventId(String clientEventId) {
        return qcRecordRepository.findByClientEventId(clientEventId);
    }

    @Override
    public QcRecord findLatestByBatchId(Long batchId) {
        return qcRecordRepository.findLatestByBatchId(batchId);
    }

    @Override
    public List<QcRecord> findByBatchId(Long batchId) {
        return qcRecordRepository.findByBatchId(batchId);
    }

    @Override
    public int countByBatchId(Long batchId) {
        return qcRecordRepository.countByBatchId(batchId);
    }
}
