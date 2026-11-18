package jakarta.ecofarm_backend.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateInfo {

    private String certType;
    private String certNo;
    private String validFrom;
    private String validTo;

    // JPQL constructor expression: đổi LocalDate sang String ngay khi chiếu từ DB
    public CertificateInfo(String certType, String certNo, LocalDate validFrom, LocalDate validTo) {
        this.certType = certType;
        this.certNo = certNo;
        this.validFrom = validFrom != null ? validFrom.toString() : null;
        this.validTo = validTo != null ? validTo.toString() : null;
    }
}
