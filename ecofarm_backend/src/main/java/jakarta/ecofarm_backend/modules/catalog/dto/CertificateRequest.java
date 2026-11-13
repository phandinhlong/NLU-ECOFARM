package jakarta.ecofarm_backend.modules.catalog.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class CertificateRequest {
    @NotNull(message = "Nhà màng không được trống")
    private Long greenhouseId;

    @NotBlank(message = "Loại chứng nhận không được trống")
    @Pattern(regexp = "VIETGAP|GLOBALGAP|ORGANIC_INTERNAL", message = "Loại chứng nhận phải là VIETGAP, GLOBALGAP hoặc ORGANIC_INTERNAL")
    private String certType;

    @NotBlank(message = "Số chứng nhận không được trống")
    @Size(max = 64, message = "Số chứng nhận tối đa 64 ký tự")
    private String certNo;

    @NotBlank(message = "Ngày hiệu lực không được trống")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Ngày hiệu lực phải có định dạng yyyy-MM-dd")
    private String validFrom;

    @NotBlank(message = "Ngày hết hạn không được trống")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Ngày hết hạn phải có định dạng yyyy-MM-dd")
    private String validTo;

    @Size(max = 255, message = "Đường dẫn tệp PDF tối đa 255 ký tự")
    private String fileKey;
}
