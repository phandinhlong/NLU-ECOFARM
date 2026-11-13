package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "qr_scan_logs")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class QrScanLog extends BaseEntity {

    // JPA relation thay cho DB foreign key (qr_codes.id)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "qr_code_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    private QrCode qrCode;

    @Column(name = "ip_addr")
    private String ipAddr;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "scanned_at", nullable = false)
    private Long scannedAt;

    @Override
    public String toString() {
        return "QrScanLog{id=" + getId() + ", scannedAt=" + scannedAt + "}";
    }
}
