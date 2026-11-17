package jakarta.ecofarm_backend.modules.log.facade;

import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.modules.log.EventRequests.AmendRequest;
import jakarta.ecofarm_backend.modules.log.EventRequests.RejectRequest;

/**
 * Các thao tác duyệt/đính chính một sự kiện đã ghi: duyệt (approve), từ chối (reject), đính chính
 * (amend). Resource JAX-RS chỉ chuyển đổi HTTP sang các lời gọi này; logic nghiệp vụ, khóa dòng và
 * transaction (jakarta @Transactional) nằm trong lớp impl. Bối cảnh phân quyền được truyền tường minh.
 */
public interface EventReviewFacade {

    BaseResponse<String> approve(Long eventId, boolean engineerOrAdmin);

    BaseResponse<String> reject(Long eventId, RejectRequest request, boolean engineerOrAdmin);

    BaseResponse<String> amend(Long eventId, AmendRequest request, Long actorId, boolean engineerOrAdmin);
}
