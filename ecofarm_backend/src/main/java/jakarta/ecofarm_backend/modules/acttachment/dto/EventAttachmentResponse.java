package jakarta.ecofarm_backend.modules.acttachment.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EventAttachmentResponse {
    private Long id;
    private Long eventId;
    private String fileKey;
    private String fileUrl;
    private String mimeType;
    private String sha256;
    private Long capturedAt;
    private BigDecimal gpsLat;
    private BigDecimal gpsLng;
    private Double gpsAccuracyM;
    private Long createdAt;
}
