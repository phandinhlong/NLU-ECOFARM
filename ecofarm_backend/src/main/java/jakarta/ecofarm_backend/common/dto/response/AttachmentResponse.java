package jakarta.ecofarm_backend.common.dto.response;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.EventAttachment;

/** Projection phản hồi cho một tệp đính kèm sự kiện. */
public record AttachmentResponse(Long id, String fileKey, String sha256, String mimeType) {

    public static AttachmentResponse from(EventAttachment attachment) {
        return new AttachmentResponse(attachment.getId(), attachment.getFileKey(),
                attachment.getSha256(), attachment.getMimeType());
    }
}
