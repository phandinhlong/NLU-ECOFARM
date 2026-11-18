package jakarta.ecofarm_backend.common.dto.request;

import jakarta.ecofarm_backend.common.enums.EventType;
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
    private String clientEventId;
    private Long occurredAt;
    @Size(max = 50, message = "Dữ liệu sự kiện tối đa 50 trường")
    private Map<String, Object> data;
    @Positive
    private Long actorId;
}
