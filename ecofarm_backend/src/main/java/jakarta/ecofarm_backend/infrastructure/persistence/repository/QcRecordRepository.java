package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.QcRecord;

import java.util.List;

public interface QcRecordRepository {
    void persist(QcRecord record);

    QcRecord merge(QcRecord record);

    QcRecord findById(Long id);

    List<QcRecord> findByBatchId(Long batchId);

    QcRecord findLatestByBatchId(Long batchId);

    int countByBatchId(Long batchId);

    QcRecord findByClientEventId(String clientEventId);
}
