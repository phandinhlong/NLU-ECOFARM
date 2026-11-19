package jakarta.ecofarm_backend.common.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class CreateBatchRequest {

    @NotNull(message = "Nhà màng không được trống")
    @Positive(message = "Invalid greenhouse id")
    private Long greenhouseId;

    @NotNull(message = "Giống không được trống")
    @Positive(message = "Invalid variety id")
    private Long varietyId;


    @NotBlank(message = "Sow date must not be blank")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Ngày gieo phải có dạng yyyy-MM-dd")
    private String sowDate;
}