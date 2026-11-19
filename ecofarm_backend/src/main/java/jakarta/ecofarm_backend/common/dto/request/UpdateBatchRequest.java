package jakarta.ecofarm_backend.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class UpdateBatchRequest {

    @NotBlank(message = "Ngày gieo không được trống")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Ngày gieo phải có dạng yyyy-MM-dd")
    private String sowDate;

    @Positive
    private Long version;
}
