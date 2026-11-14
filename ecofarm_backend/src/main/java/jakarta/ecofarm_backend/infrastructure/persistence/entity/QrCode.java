package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "qr_codes")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class QrCode extends BaseEntity {

    // JPA relation thay cho DB foreign key (packages.id)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "package_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    private PackageEntity pkg;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "serial")
    private Integer serial;

    @Column(name = "status")
    private String status;

    @Column(name = "printed_at")
    private Long printedAt;

    @Column(name = "scan_count", nullable = false)
    private Integer scanCount = 0;

    @Column(name = "last_scanned_at")
    private Long lastScannedAt;

    @Column(name = "reprint_count", nullable = false)
    private Integer reprintCount = 0;
    @Builder.Default
    private List<QrCode> qrCodes = new ArrayList<>();
    @Override
    public String toString() {
        return "QrCode{id=" + getId() + ", code='" + code + "', serial=" + serial + "}";
    }
}
