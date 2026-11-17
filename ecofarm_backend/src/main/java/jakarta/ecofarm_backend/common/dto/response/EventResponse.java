package jakarta.ecofarm_backend.common.dto.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;

import java.util.List;

/** Projection phản hồi cho một sự kiện nhật ký canh tác kèm danh sách tệp đính kèm. */
public record EventResponse(Long id, Long batchId, Integer seq, String eventType, JsonNode payload, String eventHash,
        String status, Long recordedBy, Long recordedAt, Long occurredAt, Long deviceClockSkewMs, Boolean needsReview,
        String clientEventId, Long amendsEventId, List<AttachmentResponse> attachments) {

    private static final ObjectMapper JSON = new ObjectMapper();

    public static EventResponse from(CultivationEvent event, List<AttachmentResponse> attachments) {
        JsonNode payload;
        try {
            payload = event.getPayloadJson() == null ? null : JSON.readTree(event.getPayloadJson());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Payload sự kiện đã lưu không hợp lệ", e);
        }
        return new EventResponse(event.getId(), event.getBatch().getId(), event.getSeq(), event.getEventType(), payload,
                event.getEventHash(), event.getStatus(),
                event.getRecordedBy() == null ? null : event.getRecordedBy().getId(),
                event.getRecordedAt(), event.getOccurredAt(), event.getDeviceClockSkewMs(), event.getNeedsReview(),
                event.getClientEventId(),
                event.getAmendsEvent() == null ? null : event.getAmendsEvent().getId(), attachments);
    }
}
