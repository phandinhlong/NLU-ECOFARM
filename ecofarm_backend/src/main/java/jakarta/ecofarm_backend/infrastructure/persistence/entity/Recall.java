package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "recalls")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Recall extends BaseEntity {

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    // JPA relations thay cho DB foreign keys (batches.id, users.id)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "batch_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    private Batch batch;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "created_by", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    private User createdByUser;

    @Column(name = "revoke_tx_hash", nullable = true)
    private String revokeTxHash;

    @Override
    public String toString() {
        return "Recall{id=" + getId() + ", reason='" + reason + "'}";
    }
}
