package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "packages")
public class ProductPackage {

    @Id
    private Long id;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "package_code")
    private String packageCode;

    @Column(name = "unit_count")
    private Integer unitCount;

    @Column(name = "packed_by")
    private Long packedBy;

    @Column(name = "packed_at")
    private Long packedAt;

    public ProductPackage() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getPackageCode() { return packageCode; }
    public void setPackageCode(String packageCode) { this.packageCode = packageCode; }
    public Integer getUnitCount() { return unitCount; }
    public void setUnitCount(Integer unitCount) { this.unitCount = unitCount; }
    public Long getPackedBy() { return packedBy; }
    public void setPackedBy(Long packedBy) { this.packedBy = packedBy; }
    public Long getPackedAt() { return packedAt; }
    public void setPackedAt(Long packedAt) { this.packedAt = packedAt; }
}
