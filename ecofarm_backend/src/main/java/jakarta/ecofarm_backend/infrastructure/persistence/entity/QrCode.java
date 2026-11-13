package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "qr_codes")
@Getter
@Setter
@NoArgsConstructor
public class QrCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "package_id")
    private Long packageId;

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

    @Column(name = "is_active")
    private Boolean active;

    @Version
    @Column(name = "version")
    private Integer version;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;
}
