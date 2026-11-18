package jakarta.ecofarm_backend.modules.acttachment.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.FormParam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Request body (multipart/form-data) to create an EventAttachment.
 * Injected using {@code @BeanParam} with individual {@code @FormParam} annotations.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CreateEventAttachmentRequest {

    /** CultivationEvent ID to which this attachment belongs. */
    @FormParam("eventId")
    @NotNull(message = "eventId is required")
    @Positive(message = "eventId must be positive")
    private Long eventId;

    /** Device capture epoch timestamp in millis (optional). */
    @FormParam("capturedAt")
    private Long capturedAt;

    @FormParam("gpsLat")
    @DecimalMin(value = "-90.0000000", message = "gpsLat must be between -90 and 90")
    @DecimalMax(value = "90.0000000",  message = "gpsLat must be between -90 and 90")
    private BigDecimal gpsLat;

    @FormParam("gpsLng")
    @DecimalMin(value = "-180.0000000", message = "gpsLng must be between -180 and 180")
    @DecimalMax(value = "180.0000000",  message = "gpsLng must be between -180 and 180")
    private BigDecimal gpsLng;

    @FormParam("gpsAccuracyM")
    private Double gpsAccuracyM;
}
