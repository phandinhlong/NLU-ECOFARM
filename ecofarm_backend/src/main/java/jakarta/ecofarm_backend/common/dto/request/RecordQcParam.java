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
public class RecordQcParam {

    @NotNull(message = "Mã lô không được để trống")
    @Positive(message = "Mã lô phải là số nguyên dương")
    private Long batchId;

    @NotNull(message = "Dữ liệu QC không được để trống")
    @Valid
    private RecordQcRequest request;

    @NotNull(message = "Mã người thực hiện không được để trống")
    @Positive(message = "Mã người thực hiện phải là số nguyên dương")
    private Long actorId;

    private String clientIp;
}
