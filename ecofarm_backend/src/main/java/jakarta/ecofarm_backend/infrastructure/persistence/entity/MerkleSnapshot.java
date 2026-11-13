package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "merkle_snapshots")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class MerkleSnapshot extends BaseEntity {

    @Column(name = "merkle_root", nullable = false)
    private String merkleRoot;

    @Column(name = "leaf_count", nullable = false)
    private int leafCount;

    // JPA relation thay cho DB foreign key (batches.id)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "batch_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    private Batch batch;

    @Override
    public String toString() {
        return "MerkleSnapshot{id=" + getId() + ", merkleRoot='" + merkleRoot + "', leafCount=" + leafCount + "}";
    }
}
