package jakarta.ecofarm_backend.common.dto.request;

import jakarta.ecofarm_backend.common.enums.EventType;
import jakarta.ecofarm_backend.common.enums.OutboxTarget;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventRecordCommand {

    @Positive
    private Long batchId;
    private EventType eventType;
    @Size(max = 64, message = "clientEventId must be at most 64 characters")
    private String clientEventId;
    @Positive
    private Long occurredAt;
    @Size(max = 50, message = "Event data must have at most 50 fields")
    private Map<String, Object> data;
    @Positive
    private Long actorId;
    @Positive
    private Integer schemaVersion;
    private OutboxTarget targetChain;
    @Positive
    private Long amendsEventId;
}
