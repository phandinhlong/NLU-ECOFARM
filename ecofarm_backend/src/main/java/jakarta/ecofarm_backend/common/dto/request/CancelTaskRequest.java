package jakarta.ecofarm_backend.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CancelTaskRequest {

    @NotBlank
    private String reason;
}
