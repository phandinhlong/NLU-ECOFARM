package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "certificates")
@Getter
@Setter
@NoArgsConstructor
public class Certificate extends BaseEntity {

    @Column(name = "greenhouse_id")
    private Long greenhouseId;

    @Column(name = "cert_type", nullable = false, length = 32)
    private String certType;

    @Column(name = "cert_no", nullable = false, unique = true, length = 64)
    private String certNo;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "file_key", length = 255)
    private String fileKey;
}
