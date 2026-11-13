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

    @Override
    public String toString() {
        return "QcRecord{id=" + getId() + ", brix=" + brix + ", passed=" + passed + "}";
    }
}
