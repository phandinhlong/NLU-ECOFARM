package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "qr_scan_logs")
@Getter
@Setter
@NoArgsConstructor
public class QrScanLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "qr_code_id", nullable = false)
    private Long qrCodeId;

    @Column(name = "ip_addr")
    private String ipAddr;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "scanned_at", nullable = false)
    private Long scannedAt;
}
