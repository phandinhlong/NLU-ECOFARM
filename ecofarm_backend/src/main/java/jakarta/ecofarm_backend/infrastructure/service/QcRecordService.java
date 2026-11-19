package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.QcRecord;

import java.util.List;

public interface QcRecordService {

    void persist(QcRecord record);

    QcRecord merge(QcRecord record);

    QcRecord findById(Long id);

    QcRecord findByClientEventId(String clientEventId);

    QcRecord findLatestByBatchId(Long batchId);

    List<QcRecord> findByBatchId(Long batchId);

    int countByBatchId(Long batchId);
}
