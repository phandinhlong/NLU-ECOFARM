package jakarta.ecofarm_backend.modules.acttachment.facade;

import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.modules.acttachment.dto.CreateEventAttachmentRequest;
import jakarta.servlet.http.Part;

public interface EventAttachmentFacade {

    /**
     * Create a new EventAttachment.
     * Only workers assigned to the batch (BatchAssignment) are allowed to perform this operation.
     *
     * @param request metadata accompanying the file (eventId, GPS, capturedAt, etc.)
     * @param file    uploaded file from multipart/form-data
     * @return BaseResponse containing boolean result (true if successful)
     */
    BaseResponse<Boolean> createAttachment(CreateEventAttachmentRequest request, Part file);
}
