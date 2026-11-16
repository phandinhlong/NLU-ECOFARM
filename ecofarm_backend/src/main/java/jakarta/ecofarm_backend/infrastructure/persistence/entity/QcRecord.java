package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "qc_records")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class QcRecord extends BaseEntity {

    // JPA relations thay cho DB foreign keys (batches.id, users.id)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "batch_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    private Batch batch;

    @Column(name = "brix")
    private Double brix;

    @Column(name = "avg_weight_kg")
    private Double avgWeightKg;

    @Column(name = "passed")
    private Boolean passed;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "inspected_by", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    private User inspectedByUser;

    @Column(name = "inspected_at")
    private Long inspectedAt;

    @Column(name = "size_description")
    private String sizeDescription;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // Thresholds actually used when the server computed the result, kept for audit and for
    // detecting a stale pass when the isolation basis later changes.
    @Column(name = "threshold_brix_used")
    private Double thresholdBrixUsed;

    @Column(name = "min_isolation_days_used")
    private Integer minIsolationDaysUsed;

    @Column(name = "isolation_eligible_at")
    private Long isolationEligibleAt;

    @Column(name = "harvested_at_used")
    private Long harvestedAtUsed;

    // QC_PASSED or REJECTED.
    @Column(name = "status")
    private String status;

    // Set true when a later log back-fill/amendment invalidated this result; kept for history.
    @Column(name = "superseded")
    private Boolean superseded;

    @Override
    public String toString() {
        return "QcRecord{id=" + getId() + ", brix=" + brix + ", passed=" + passed + "}";
    }
}
