package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
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

    /**
     * Lấy sự kiện kèm khóa ghi bi quan, dùng cho luồng duyệt/đính chính. Phải nằm trong transaction.
     */
    public CultivationEvent findByIdForUpdate(Long id) {
        return em.find(CultivationEvent.class, id, LockModeType.PESSIMISTIC_WRITE);
    }

    /**
     * Đếm số sự kiện duyệt (EVENT_APPROVED) tham chiếu tới một sự kiện gốc, để approve idempotent.
     */
    public long countApprovalsByAmendsEventId(Long eventId, String approvedTypeCode) {
        return em.createQuery(
                        "select count(e) from CultivationEvent e where e.amendsEvent.id = :id and e.eventType = :t",
                        Long.class)
                .setParameter("id", eventId).setParameter("t", approvedTypeCode)
                .getSingleResult();
    }

    private static String filterWhere(String status, String eventType, boolean includeSuperseded) {
        return " where e.batch.id = :batchId and e.isActive = true"
                + (status == null ? "" : " and e.status = :status")
                + (eventType == null ? "" : " and e.eventType = :eventType")
                + (includeSuperseded ? "" : " and e.status <> 'SUPERSEDED'");
    }

    /** Đếm sự kiện của một lô theo bộ lọc status/eventType/includeSuperseded. */
    public long countByBatchFiltered(Long batchId, String status, String eventType, boolean includeSuperseded) {
        var q = em.createQuery("select count(e) from CultivationEvent e"
                + filterWhere(status, eventType, includeSuperseded), Long.class)
                .setParameter("batchId", batchId);
        if (status != null) q.setParameter("status", status);
        if (eventType != null) q.setParameter("eventType", eventType);
        return q.getSingleResult();
    }

    /** Một trang sự kiện của một lô theo bộ lọc, sắp theo seq rồi id. */
    public List<CultivationEvent> findPageByBatchFiltered(Long batchId, String status, String eventType,
            boolean includeSuperseded, int offset, int limit) {
        var q = em.createQuery("select e from CultivationEvent e"
                + filterWhere(status, eventType, includeSuperseded)
                + " order by e.seq asc, e.id asc", CultivationEvent.class)
                .setParameter("batchId", batchId);
        if (status != null) q.setParameter("status", status);
        if (eventType != null) q.setParameter("eventType", eventType);
        return q.setFirstResult(offset).setMaxResults(limit).getResultList();
    }
}
