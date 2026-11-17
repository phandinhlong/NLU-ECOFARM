package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class CultivationEventRepository {

    @PersistenceContext
    private EntityManager em;

    public List<CultivationEvent> findByBatchIdOrderBySeqAsc(Long batchId) {
        return em.createQuery(
                        "select e from CultivationEvent e where e.batch.id = :batchId and (e.isActive is null or e.isActive = true) order by e.seq asc, e.id asc",
                        CultivationEvent.class)
                .setParameter("batchId", batchId)
                .getResultList();
    }

    /**
     * Các sự kiện đang hoạt động của một lô, dùng để tính lại cửa sổ cách ly cho QC.
     */
    public List<CultivationEvent> findActiveByBatchId(Long batchId) {
        return em.createQuery(
                        "select e from CultivationEvent e where e.batch.id = :batchId and e.isActive = true",
                        CultivationEvent.class)
                .setParameter("batchId", batchId)
                .getResultList();
    }

    public void persist(CultivationEvent event) {
        em.persist(event);
    }

    /**
     * Số thứ tự (seq) tiếp theo trong nhật ký của một lô (bằng 1 khi lô chưa có sự kiện nào).
     */
    public int nextSeq(Long batchId) {
        Integer maxSeq = em.createQuery(
                        "select max(e.seq) from CultivationEvent e where e.batch.id = :batchId", Integer.class)
                .setParameter("batchId", batchId)
                .getSingleResult();
        return maxSeq == null ? 1 : maxSeq + 1;
    }

    /**
     * Các sự kiện đã ghi theo id do client sinh ra, dùng để phát hiện retry idempotent.
     */
    public List<CultivationEvent> findByClientEventId(String clientEventId) {
        return em.createQuery(
                        "select e from CultivationEvent e where e.clientEventId = :cid", CultivationEvent.class)
                .setParameter("cid", clientEventId)
                .getResultList();
    }
}
