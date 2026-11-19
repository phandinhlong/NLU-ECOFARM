package jakarta.ecofarm_backend.modules.catalog.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class VarietyRequest {
    @NotBlank(message = "Variety code must not be blank")
    @Size(max = 32, message = "Variety code must be at most 32 characters")
    private String code;

    @NotBlank(message = "Variety name must not be blank")
    @Size(max = 128, message = "Variety name must be at most 128 characters")
    private String name;

    @Min(value = 1, message = "Số ngày từ thụ phấn đến thu hoạch phải lớn hơn 0")
    @Max(value = 365, message = "Số ngày từ thụ phấn đến thu hoạch tối đa 365")
    private Integer daysPollinationToHarvest;

    @DecimalMin(value = "0", message = "Ngưỡng Brix tối thiểu phải lớn hơn hoặc bằng 0")
    @DecimalMax(value = "30", message = "Ngưỡng Brix tối thiểu tối đa là 30")
    @Digits(integer = 2, fraction = 1, message = "Ngưỡng Brix tối đa 1 chữ số thập phân")
    private BigDecimal minBrix;

    @Min(value = 0, message = "Số ngày cách ly tối thiểu phải lớn hơn hoặc bằng 0")
    @Max(value = 365, message = "Số ngày cách ly tối thiểu tối đa 365")
    private Integer minIsolationDays;

    private Boolean active;
    @Positive
    private Long version;
}
