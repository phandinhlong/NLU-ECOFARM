package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.common.dto.request.PageCriteria;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.BatchAssignment;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;

import java.util.List;
import java.util.Map;

/**
 * Lớp giả (test double) cơ sở cho {@link BatchRepository}. Mọi hàm trả giá trị mặc định vô hại để
 * mỗi test chỉ cần kế thừa và ghi đè đúng hành vi mà nó kiểm tra.
 */
public abstract class StubBatchRepository implements BatchRepository {
    @Override public List<Batch> findPage(PageCriteria criteria) { return List.of(); }
    @Override public Batch findById(Long id) { return null; }
    @Override public Batch findByIdForUpdate(Long id) { return null; }
    @Override public Batch findByBatchCode(String batchCode) { return null; }
    @Override public Batch merge(Batch batch) { return batch; }
    @Override public void persist(Batch batch) { }
    @Override public Boolean existsByBatchCode(String batchCode) { return false; }
    @Override public Long countEvents(Long batchId) { return 0L; }
    @Override public Long findPollinationDate(Long batchId) { return null; }
    @Override public Map<Long, Long> findPollinationDates(List<Long> batchIds) { return Map.of(); }
    @Override public List<BatchAssignment> findAssignments(Long batchId) { return List.of(); }
    @Override public Map<Long, Long> countAssignmentsByBatchIds(List<Long> batchIds) { return Map.of(); }
    @Override public Boolean isAssigned(Long batchId, Long userId) { return true; }
    @Override public List<User> findAssignedWorkerPage(Long batchId, Long lastIndex, int limit) { return List.of(); }
    @Override public List<Long> findAssignedUserIds(Long batchId) { return List.of(); }
    @Override public void removeAssignments(Long batchId, List<Long> userIds) { }
    @Override public void persistAssignment(BatchAssignment assignment) { }
}
