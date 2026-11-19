package jakarta.ecofarm_backend.common.dto.response;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    @Positive
    private Long id;
    @Positive
    private Long batchId;
    private Integer seq;
    private String eventType;
    private String status;
    private String reviewStatus;
    private Map<String, Object> data;
    @Size(max = 66)
    private String eventHash;
    @Positive
    private Long recordedById;
    @Size(max = 255)
    private String recordedByName;
    private LocalDateTime occurredAt;
    private LocalDateTime recordedAt;
    @Size(max = 64)
    private String clientEventId;
    @Positive
    private Long amendsEventId;
    @Size(max = 64)
    private String batchCode;
    @Positive
    private Long greenhouseId;
    @Size(max = 128)
    private String greenhouseName;
    @Positive
    private Long pendingApproverId;
    @Positive
    private Long assignerId;
    private Boolean assignerActive;
    private String rejectReason;
    private String amendReason;
    private String amendNote;
}
