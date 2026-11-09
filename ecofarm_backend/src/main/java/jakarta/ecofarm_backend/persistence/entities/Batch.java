package jakarta.ecofarm_backend.persistence.entities;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;

@Entity
@Table(name = "batches")
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_code", unique = true)
    private String batchCode;

    @Column(name = "greenhouse_id")
    private Long greenhouseId;

    @Column(name = "variety_id")
    private Long varietyId;

    @Column(name = "status")
    private String status;

    @Column(name = "sow_date")
    private LocalDate sowDate;

    @Column(name = "harvest_date")
    private LocalDate harvestDate;

    @Column(name = "salt_enc")
    private Long saltEnc;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "created_by",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    private User createdBy;

    @Column(name = "activated_at")
    private Long activatedAt;

    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    public Batch() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }
    public Long getGreenhouseId() { return greenhouseId; }
    public void setGreenhouseId(Long greenhouseId) { this.greenhouseId = greenhouseId; }
    public Long getVarietyId() { return varietyId; }
    public void setVarietyId(Long varietyId) { this.varietyId = varietyId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getSowDate() { return sowDate; }
    public void setSowDate(LocalDate sowDate) { this.sowDate = sowDate; }
    public LocalDate getHarvestDate() { return harvestDate; }
    public void setHarvestDate(LocalDate harvestDate) { this.harvestDate = harvestDate; }
    public Long getSaltEnc() { return saltEnc; }
    public void setSaltEnc(Long saltEnc) { this.saltEnc = saltEnc; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public Long getActivatedAt() { return activatedAt; }
    public void setActivatedAt(Long activatedAt) { this.activatedAt = activatedAt; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Batch{id=" + id + ", batchCode='" + batchCode + "', status='" + status + "'}";
    }
}
