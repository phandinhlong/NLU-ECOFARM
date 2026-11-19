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
    @Size(max = 64, message = "clientEventId tối đa 64 ký tự")
    private String clientEventId;

    @NotNull
    private EventType eventType;

    private Integer schemaVersion;

    @NotBlank
    private String occurredAt;

    @Size(max = 50, message = "Dữ liệu sự kiện tối đa 50 trường")
    private Map<String, Object> data;
}
