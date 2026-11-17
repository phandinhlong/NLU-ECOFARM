package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.EventAttachment;

import java.util.List;

/**
 * Lưu trữ cho tệp đính kèm của sự kiện nhật ký. Theo quy ước interface + impl (xem UserRepository).
 */
public interface EventAttachmentRepository {

    void persist(EventAttachment attachment);

    EventAttachment merge(EventAttachment attachment);

    /** Tệp đính kèm đang hoạt động theo fileKey (rỗng nếu không có). */
    List<EventAttachment> findActiveByFileKey(String fileKey);

    /** Tệp đính kèm chưa gắn với sự kiện nào theo fileKey, dùng khi tạo sự kiện tham chiếu tới nó. */
    List<EventAttachment> findUnlinkedByFileKey(String fileKey);

    /** Các giá trị sha256 của những tệp đính kèm có fileKey nằm trong danh sách. */
    List<String> findSha256ByFileKeys(List<String> fileKeys);

    /** Tệp đính kèm đang hoạt động của một sự kiện, sắp theo id. */
    List<EventAttachment> findActiveByEventIdOrderById(Long eventId);

    /** Tệp đính kèm đang hoạt động của nhiều sự kiện, sắp theo id. */
    List<EventAttachment> findActiveByEventIdsOrderById(List<Long> eventIds);
}
