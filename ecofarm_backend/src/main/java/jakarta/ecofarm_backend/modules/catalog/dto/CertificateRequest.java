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
    @Positive
    private Long greenhouseId;

    @NotBlank(message = "Certificate type must not be blank")
    @Pattern(regexp = "VIETGAP|GLOBALGAP|ORGANIC_INTERNAL", message = "Loại chứng nhận phải là VIETGAP, GLOBALGAP hoặc ORGANIC_INTERNAL")
    private String certType;

    @NotBlank(message = "Certificate number must not be blank")
    @Size(max = 64, message = "Certificate number must be at most 64 characters")
    private String certNo;

    @NotBlank(message = "Valid from date must not be blank")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Ngày hiệu lực phải có định dạng yyyy-MM-dd")
    private String validFrom;

    @NotBlank(message = "Valid to date must not be blank")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Ngày hết hạn phải có định dạng yyyy-MM-dd")
    private String validTo;

    @Size(max = 255, message = "PDF file URL must be at most 255 characters")
    private String fileKey;

    private Boolean active;
    @Positive
    private Long version;
}
