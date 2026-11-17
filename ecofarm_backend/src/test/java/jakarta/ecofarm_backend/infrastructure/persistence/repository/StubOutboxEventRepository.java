package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent;

import java.util.List;

/**
 * Lớp giả (test double) cơ sở cho {@link OutboxEventRepository}. Mọi hàm là no-op/mặc định để
 * mỗi test chỉ cần kế thừa và ghi đè đúng hành vi mà nó kiểm tra.
 */
public abstract class StubOutboxEventRepository implements OutboxEventRepository {
    @Override public void persist(OutboxEvent event) { }
    @Override public OutboxEvent findById(Long id) { return null; }
    @Override public List<OutboxEvent> findPending() { return List.of(); }
    @Override public void incrementAttempts(OutboxEvent event) { }
    @Override public void complete(OutboxEvent event) { }
    @Override public void markFailed(OutboxEvent event, String errorMsg) { }
    @Override public boolean existsProcessedByBatchIdHashAndSeq(String batchIdHash, int seq) { return false; }
    @Override public int findNextSeqByBatchIdHash(String batchIdHash) { return 1; }
}
