package jakarta.ecofarm_backend.common.dto.request;

import jakarta.ecofarm_backend.common.enums.EventType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateTaskRequest {

    @Positive
    @NotNull
    private Long workerId;

    @NotNull
    private EventType taskType;

    private String note;
}
