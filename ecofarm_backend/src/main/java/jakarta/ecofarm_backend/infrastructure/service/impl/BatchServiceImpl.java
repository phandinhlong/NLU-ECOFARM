package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.BatchAssignment;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.BatchRepository;
import jakarta.ecofarm_backend.infrastructure.service.BatchService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class BatchServiceImpl implements BatchService {

    @Inject
    private BatchRepository batchRepository;

    @Override
    public List<Batch> findPage(PageCriteria criteria) {
        return this.batchRepository.findPage(criteria);
    }

    @Override
    public Batch findById(Long id) {
        return this.batchRepository.findById(id);
    }

    @Override
    public Batch findByBatchCode(String batchCode) {
        return this.batchRepository.findByBatchCode(batchCode);
    }

    @Override
    public Batch merge(Batch batch) {
        return this.batchRepository.merge(batch);
    }

    @Override
    public void persist(Batch batch) {
        this.batchRepository.persist(batch);
    }

    @Override
    public Boolean existsByBatchCode(String batchCode) {
        return this.batchRepository.existsByBatchCode(batchCode);
    }

    @Override
    public Long countEvents(Long batchId) {
        return this.batchRepository.countEvents(batchId);
    }

    @Override
    public Long findPollinationDate(Long batchId) {
        return this.batchRepository.findPollinationDate(batchId);
    }

    @Override
    public Map<Long, Long> findPollinationDates(List<Long> batchIds) {
        return this.batchRepository.findPollinationDates(batchIds);
    }

    @Override
    public List<BatchAssignment> findAssignments(Long batchId) {
        return this.batchRepository.findAssignments(batchId);
    }

    @Override
    public Map<Long, Long> countAssignmentsByBatchIds(List<Long> batchIds) {
        return this.batchRepository.countAssignmentsByBatchIds(batchIds);
    }

    @Override
    public Boolean isAssigned(Long batchId, Long userId) {
        return this.batchRepository.isAssigned(batchId, userId);
    }

    @Override
    public List<User> findAssignedWorkerPage(Long batchId, Long lastIndex, int limit) {
        return this.batchRepository.findAssignedWorkerPage(batchId, lastIndex, limit);
    }

    @Override
    public List<Long> findAssignedUserIds(Long batchId) {
        return this.batchRepository.findAssignedUserIds(batchId);
    }

    @Override
    public void removeAssignments(Long batchId, List<Long> userIds) {
        this.batchRepository.removeAssignments(batchId, userIds);
    }

    @Override
    public void persistAssignment(BatchAssignment assignment) {
        this.batchRepository.persistAssignment(assignment);
    }
}
