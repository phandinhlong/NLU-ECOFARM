package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent;

import java.util.List;

public interface OutboxEventRepository {
    void persist(OutboxEvent event);

    OutboxEvent findById(Long id);

    List<OutboxEvent> findPending();

    void incrementAttempts(OutboxEvent event);

    void complete(OutboxEvent event);

    void markFailed(OutboxEvent event, String errorMsg);

    boolean existsProcessedByBatchIdHashAndSeq(String batchIdHash, int seq);

    int findNextSeqByBatchIdHash(String batchIdHash);
}
