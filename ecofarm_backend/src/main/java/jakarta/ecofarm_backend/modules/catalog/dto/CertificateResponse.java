package jakarta.ecofarm_backend.modules.catalog.dto;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Certificate;
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

    public static CertificateResponse from(Certificate c) {
        return CertificateResponse.builder()
                .id(c.getId())
                .greenhouseId(c.getGreenhouse() == null ? null : c.getGreenhouse().getId())
                .certType(c.getCertType())
                .certNo(c.getCertNo())
                .validFrom(c.getValidFrom() == null ? null : c.getValidFrom().toString())
                .validTo(c.getValidTo() == null ? null : c.getValidTo().toString())
                .fileKey(c.getFileKey())
                .active(c.isActive())
                .version(c.getVersion())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .downloadUrl(null)
                .build();
    }
}
