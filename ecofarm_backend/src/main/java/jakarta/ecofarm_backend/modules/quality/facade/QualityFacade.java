package jakarta.ecofarm_backend.modules.quality.facade;

import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.BatchResponse;
import jakarta.ecofarm_backend.common.dto.response.QcResponse;
import jakarta.ecofarm_backend.modules.quality.QualityRequests.HarvestRequest;
import jakarta.ecofarm_backend.modules.quality.QualityRequests.QcRequest;

import java.util.List;

/**
 * Các thao tác nghiệp vụ cho thu hoạch và kiểm định chất lượng trên một lô. Resource JAX-RS chỉ
 * chuyển đổi HTTP sang các lời gọi này; toàn bộ lưu trữ, khóa dòng, tính lại cách ly và đẩy outbox
 * đều nằm ở đây.
 *
 * <p>Bối cảnh phân quyền mà resource lấy từ request được truyền vào tường minh
 * ({@code actorId}, {@code engineerOrAdmin}) để facade không đính kèm các kiểu của tầng web.
 */
public interface QualityFacade {

    BaseResponse<BatchResponse> harvest(Long batchId, HarvestRequest request, Long actorId, boolean engineerOrAdmin);

    /**
     * Ghi một kết quả QC. Server tự tính lại đạt/không đạt từ ngưỡng của giống và lịch sử sự kiện
     * thật; {@code confirmPassed} chỉ là bước ký xác nhận cuối của kỹ sư. Vi phạm quy tắc (thiếu
     * ngưỡng, cách ly chưa đủ) được ném dưới dạng ConflictException và map sang HTTP qua mapper chung.
     */
    BaseResponse<QcResponse> recordQc(Long batchId, QcRequest request, Long actorId);

    /** Chỉ đọc: các bản ghi QC đang hoạt động của một lô, tính lại pass quá hạn ngay tại chỗ dùng. */
    BaseResponse<List<QcResponse>> listQc(Long batchId);
}
