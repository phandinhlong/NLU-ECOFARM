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

    @Column(name = "attempt_number")
    private Integer attemptNumber;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "deficit_days")
    private Integer deficitDays;

    @Column(name = "sample_brix", columnDefinition = "TEXT")
    private String sampleBrix;

    @Column(name = "sample_weights", columnDefinition = "TEXT")
    private String sampleWeights;

    @Column(name = "status")
    private String status;

    @Column(name = "admin_decision")
    private String adminDecision;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "reviewed_by", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    private User reviewedByUser;

    @Column(name = "reviewed_at")
    private Long reviewedAt;

    @Column(name = "client_event_id")
    private String clientEventId;

    @Column(name = "photo_hashes", columnDefinition = "TEXT")
    private String photoHashes;

    @Column(name = "system_suggested_passed")
    private Boolean systemSuggestedPassed;

    @Column(name = "criteria_evaluation", columnDefinition = "TEXT")
    private String criteriaEvaluation;

    @Override
    public String toString() {
        return "QcRecord{id=" + getId() + ", brix=" + brix + ", passed=" + passed + "}";
    }
}
