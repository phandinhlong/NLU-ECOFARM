package jakarta.ecofarm_backend.common.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExtraQrRequest {
    @NotNull(message = "extraCount must not be null")
    @Min(value = 1, message = "extraCount must be at least 1")
    @Max(value = 10000, message = "extraCount must be at most 10000")
    private Integer extraCount;

    @NotBlank(message = "reason must not be blank")
    @Size(max = 500, message = "reason must be at most 500 characters")
    private String reason;
}
