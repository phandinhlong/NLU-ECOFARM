package jakarta.ecofarm_backend.modules.batch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    private Long greenhouseId;

    @NotNull(message = "Giống không được trống")
    private Long varietyId;

    @NotBlank(message = "Ngày gieo không được trống")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Ngày gieo phải có dạng yyyy-MM-dd")
    private String sowDate;
}