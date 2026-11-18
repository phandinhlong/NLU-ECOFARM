package jakarta.ecofarm_backend.common.dto.response;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CertificateResponse {
    @Positive
    private Long id;
    @Positive
    private Long greenhouseId;
    private String certType;
    private String certNo;
    private String validFrom;
    private String validTo;
    private String fileKey;
    private Boolean active;
    @Positive
    private Long version;
    @Positive
    private Long createdAt;
    @Positive
    private Long updatedAt;
    private String downloadUrl;
}
