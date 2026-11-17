package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.common.dto.request.PageCriteria;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.BatchAssignment;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;

import java.util.List;
import java.util.Map;

public interface BatchService {

    List<Batch> findPage(PageCriteria criteria);

    Batch findById(Long id);

    Batch findByBatchCode(String batchCode);

    Batch merge(Batch batch);

    void persist(Batch batch);

    Boolean existsByBatchCode(String batchCode);

    Long countEvents(Long batchId);

    Long findPollinationDate(Long batchId);

    Map<Long, Long> findPollinationDates(List<Long> batchIds);

    List<BatchAssignment> findAssignments(Long batchId);

    Map<Long, Long> countAssignmentsByBatchIds(List<Long> batchIds);

    Boolean isAssigned(Long batchId, Long userId);

    List<User> findAssignedWorkerPage(Long batchId, Long lastIndex, int limit);

    List<Long> findAssignedUserIds(Long batchId);

    void removeAssignments(Long batchId, List<Long> userIds);

    void persistAssignment(BatchAssignment assignment);
}
