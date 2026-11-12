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
public class GreenhouseRequest {
    @NotBlank(message = "Mã nhà màng không được trống")
    @Size(max = 32, message = "Mã nhà màng tối đa 32 ký tự")
    private String code;

    @NotBlank(message = "Tên nhà màng không được trống")
    @Size(max = 128, message = "Tên nhà màng tối đa 128 ký tự")
    private String name;

    @DecimalMin(value = "-90", message = "Vĩ độ tối thiểu là -90")
    @DecimalMax(value = "90", message = "Vĩ độ tối đa là 90")
    private BigDecimal gpsLat;

    @DecimalMin(value = "-180", message = "Kinh độ tối thiểu là -180")
    @DecimalMax(value = "180", message = "Kinh độ tối đa là 180")
    private BigDecimal gpsLng;

    @DecimalMin(value = "0", message = "Diện tích phải lớn hơn hoặc bằng 0")
    private BigDecimal areaM2;

    @Pattern(regexp = "ACTIVE|INACTIVE|MAINTENANCE", message = "Trạng thái phải là ACTIVE, INACTIVE hoặc MAINTENANCE")
    private String status;

    private Long version;
}
