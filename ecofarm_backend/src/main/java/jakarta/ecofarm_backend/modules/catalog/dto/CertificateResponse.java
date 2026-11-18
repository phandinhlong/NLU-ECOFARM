package jakarta.ecofarm_backend.modules.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CertificateResponse {
    private Long id;
    private Long greenhouseId;
    private String certType;
    private String certNo;
    private String validFrom;
    private String validTo;
    private String fileKey;
    private Boolean active;
    private Long version;
    private Long createdAt;
    private Long updatedAt;
    private String downloadUrl;
}
