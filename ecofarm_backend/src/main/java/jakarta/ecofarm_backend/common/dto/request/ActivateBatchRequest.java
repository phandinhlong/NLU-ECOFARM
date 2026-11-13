package jakarta.ecofarm_backend.common.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivateBatchRequest {
    @NotNull(message = "unitCount must not be null")
    @Min(value = 1, message = "unitCount must be at least 1")
    @Max(value = 100000, message = "unitCount must be at most 100000")
    private Integer unitCount;

    private Integer version;
}
