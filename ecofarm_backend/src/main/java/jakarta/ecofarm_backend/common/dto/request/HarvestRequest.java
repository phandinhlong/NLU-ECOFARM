package jakarta.ecofarm_backend.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class HarvestRequest {

    @NotBlank(message = "clientEventId không được để trống")
    @Pattern(regexp = "^[0-9a-fA-F-]{36}$", message = "clientEventId phải là định dạng UUID hợp lệ")
    private String clientEventId;

    @Positive(message = "Thời gian thu hoạch phải là số dương")
    private Long harvestedAt;

    @Positive(message = "Số lượng ước tính phải là số nguyên dương")
    private Integer estimatedQuantity;

    private String area;

    private String note;
}
