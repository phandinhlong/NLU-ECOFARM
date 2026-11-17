package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.QcRecord;

import java.util.List;

/**
 * Lưu trữ các bản ghi QC. Theo quy ước interface + impl (xem UserRepository).
 */
public interface QcRecordRepository {

    void persist(QcRecord record);

    QcRecord merge(QcRecord record);

    /**
     * Các bản ghi QC đang hoạt động của một lô, mới nhất trước (giữ đúng truy vấn cũ trong resource).
     */
    List<QcRecord> findActiveByBatchIdOrderByIdDesc(Long batchId);
}
