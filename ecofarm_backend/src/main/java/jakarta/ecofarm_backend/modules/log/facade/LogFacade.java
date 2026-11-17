package jakarta.ecofarm_backend.modules.log.facade;

import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.AttachmentResponse;
import jakarta.ecofarm_backend.common.dto.response.EventResponse;
import jakarta.ecofarm_backend.common.dto.response.PageResponse;
import jakarta.ecofarm_backend.modules.log.EventRequests.CreateEventRequest;

import java.io.IOException;
import java.util.List;

/**
 * Các thao tác nghiệp vụ cho nhật ký canh tác (tạo sự kiện, tải lên/tải xuống tệp đính kèm, liệt kê).
 * Resource JAX-RS chỉ chuyển đổi HTTP sang các lời gọi này; logic nghiệp vụ, khóa dòng và transaction
 * nằm ở đây. Bối cảnh phân quyền (actorId, engineerOrAdmin) được truyền vào tường minh.
 */
public interface LogFacade {

    /** Dữ liệu một tệp tải lên mà resource đã đọc từ request (không dùng kiểu của tầng web). */
    record UploadInput(String mimeType, byte[] bytes) {}

    BaseResponse<List<AttachmentResponse>> upload(Long batchId, List<UploadInput> files, Long actorId, boolean engineerOrAdmin) throws IOException;

    /** Chỉ đọc: nội dung và mime của một tệp đính kèm thuộc lô. */
    record DownloadResult(byte[] bytes, String mimeType) {}

    DownloadResult download(Long batchId, String fileKey, Long actorId, boolean engineerOrAdmin) throws IOException;

    BaseResponse<EventResponse> create(Long batchId, CreateEventRequest request, Long actorId, boolean engineerOrAdmin, long clockSkewThresholdMs);

    /** Chỉ đọc: liệt kê sự kiện của một lô theo trang và bộ lọc. */
    BaseResponse<PageResponse<EventResponse>> list(Long batchId, int page, int size, String status, String eventType,
            boolean includeSuperseded, Long actorId, boolean engineerOrAdmin, boolean workerOrFarmer);
}
