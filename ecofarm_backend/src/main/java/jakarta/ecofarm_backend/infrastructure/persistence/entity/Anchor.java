package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "anchors")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Anchor extends BaseEntity {

    // JPA relation thay cho DB foreign key (merkle_snapshots.id)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "snapshot_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    private MerkleSnapshot snapshot;

    @Column(name = "chain_id")
    private Integer chainId;

    @Column(name = "contract_address")
    private String contractAddress;

    @Column(name = "tx_hash")
    private String txHash;

    @Column(name = "block_number")
    private Long blockNumber;

    @Column(name = "confirmations")
    private int confirmations;

    @Column(name = "status")
    private String status;

    @Column(name = "anchored_at")
    private Long anchoredAt; // epoch seconds (BIGINT)

    @Column(name = "explorer_url")
    private String explorerUrl;

    @Override
    public String toString() {
        return "Anchor{id=" + getId() + ", chainId=" + chainId + ", status='" + status + "'}";
    }
}
