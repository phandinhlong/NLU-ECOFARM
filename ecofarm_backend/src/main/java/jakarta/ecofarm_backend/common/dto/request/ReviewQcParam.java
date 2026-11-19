package jakarta.ecofarm_backend.common.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewQcParam {

    @NotNull(message = "Mã lô không được để trống")
    @Positive
    private Long batchId;

    @NotNull(message = "Dữ liệu kiểm duyệt không được để trống")
    @Valid
    private ReviewQcRequest request;

    @NotNull(message = "Mã Admin không được để trống")
    @Positive
    private Long adminId;

    private String clientIp;
}
