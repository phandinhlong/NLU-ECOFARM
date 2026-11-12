package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "qc_records")
public class QcRecord {

    @Id
    private Long id;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "brix")
    private BigDecimal brix;

    @Column(name = "avg_weight_kg")
    private BigDecimal avgWeightKg;

    @Column(name = "passed")
    private Boolean passed;

    @Column(name = "inspected_by")
    private Long inspectedBy;

    @Column(name = "inspected_at")
    private Long inspectedAt;

    public QcRecord() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public BigDecimal getBrix() { return brix; }
    public void setBrix(BigDecimal brix) { this.brix = brix; }
    public BigDecimal getAvgWeightKg() { return avgWeightKg; }
    public void setAvgWeightKg(BigDecimal avgWeightKg) { this.avgWeightKg = avgWeightKg; }
    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }
    public Long getInspectedBy() { return inspectedBy; }
    public void setInspectedBy(Long inspectedBy) { this.inspectedBy = inspectedBy; }
    public Long getInspectedAt() { return inspectedAt; }
    public void setInspectedAt(Long inspectedAt) { this.inspectedAt = inspectedAt; }
}
