package jakarta.ecofarm_backend.common.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RecordQcRequest {

    @NotBlank(message = "clientEventId không được trống")
    @Pattern(regexp = "^[0-9a-fA-F-]{36}$", message = "clientEventId phải có định dạng UUID hợp lệ")
    private String clientEventId;

    @NotNull(message = "brixSamples không được trống")
    @Size(min = 5, max = 5, message = "Phải nhập đúng 5 mẫu độ Brix")
    private List<@NotNull(message = "Mẫu Brix không được null") @PositiveOrZero(message = "Brix không được âm") @DecimalMax(value = "30.0", message = "Brix tối đa là 30.0") Double> brixSamples;

    @NotNull(message = "weightSamples không được trống")
    @Size(min = 5, max = 5, message = "Phải nhập đúng 5 mẫu cân nặng")
    private List<@NotNull(message = "Mẫu cân nặng không được null") @Positive(message = "Cân nặng phải là số dương") @DecimalMin(value = "0.1", message = "Cân nặng tối thiểu 0.1 kg") @DecimalMax(value = "20.0", message = "Cân nặng tối đa 20.0 kg") Double> weightSamples;

    @NotNull(message = "Kết quả đánh giá (passed) không được để trống")
    private Boolean passed;

    private List<String> photoHashes;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Ngày thu hoạch phải có dạng yyyy-MM-dd")
    private String harvestDate;

    @Size(max = 1000, message = "Ghi chú không quá 1000 ký tự")
    private String note;

    @Positive(message = "Thời gian xảy ra phải là số dương")
    private Long occurredAt;
}
