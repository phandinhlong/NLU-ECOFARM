package jakarta.ecofarm_backend.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class CompleteTaskRequest {

    @NotBlank
    private String clientEventId;

    @NotBlank
    private String occurredAt;

    @Size(max = 50, message = "Event data must have at most 50 fields")
    private Map<String, Object> data;
}
