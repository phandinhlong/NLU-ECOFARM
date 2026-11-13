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
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    @PrePersist
    protected void prePersist() {
        if (this.createdAt == null) this.createdAt = System.currentTimeMillis();
        if (this.updatedAt == null) this.updatedAt = System.currentTimeMillis();
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = System.currentTimeMillis();
    }
}
