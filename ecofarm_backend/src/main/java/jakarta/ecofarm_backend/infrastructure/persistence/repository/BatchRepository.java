package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;

@Stateless
public class BatchRepository {

    @PersistenceContext
    private EntityManager em;

    public Batch findById(Long id) {
        return em.find(Batch.class, id);
    }

    /**
     * Lấy batch kèm khóa ghi bi quan (pessimistic write lock) để các thao tác ghi harvest/QC/sự kiện
     * trên cùng một lô được tuần tự hóa. Hàm gọi phải đang nằm trong một transaction.
     */
    public Batch findByIdForUpdate(Long id) {
        return em.find(Batch.class, id, LockModeType.PESSIMISTIC_WRITE);
    }

    public Batch merge(Batch batch) {
        return em.merge(batch);
    }

    /**
     * Đếm số phân công đang hoạt động giữa một người dùng và một lô, dùng để cấp quyền cho người dùng
     * không phải kỹ sư.
     */
    public long countActiveAssignment(Long batchId, Long userId) {
        return em.createQuery(
                        "select count(a) from BatchAssignment a where a.batch.id = :batchId and a.user.id = :userId and a.isActive = true",
                        Long.class)
                .setParameter("batchId", batchId)
                .setParameter("userId", userId)
                .getSingleResult();
    }
}
