package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "certificates")
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
    private Boolean active;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    public Certificate() {
    }

    @PrePersist
    protected void prePersist() {
        if (this.createdAt == null) this.createdAt = System.currentTimeMillis();
        if (this.updatedAt == null) this.updatedAt = System.currentTimeMillis();
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = System.currentTimeMillis();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getGreenhouseId() { return greenhouseId; }
    public void setGreenhouseId(Long greenhouseId) { this.greenhouseId = greenhouseId; }
    public String getCertType() { return certType; }
    public void setCertType(String certType) { this.certType = certType; }
    public String getCertNo() { return certNo; }
    public void setCertNo(String certNo) { this.certNo = certNo; }
    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }
    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }
    public String getFileKey() { return fileKey; }
    public void setFileKey(String fileKey) { this.fileKey = fileKey; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
}
