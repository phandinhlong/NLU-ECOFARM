package jakarta.ecofarm_backend.common.dto.request;

import jakarta.ecofarm_backend.common.enums.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class CreateEventRequest {

    @NotBlank
    @Size(max = 64, message = "clientEventId must be at most 64 characters")
    private String clientEventId;

    @NotNull
    private EventType eventType;

    private Integer schemaVersion;

    @NotBlank
    private String occurredAt;

    @Size(max = 50, message = "Event data must have at most 50 fields")
    private Map<String, Object> data;
}
