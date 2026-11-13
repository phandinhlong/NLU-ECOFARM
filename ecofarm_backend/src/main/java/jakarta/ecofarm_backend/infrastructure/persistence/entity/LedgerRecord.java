package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "ledger_records")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class LedgerRecord extends BaseEntity {

    // JPA relation thay cho DB foreign key (cultivation_events.id)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "event_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    private CultivationEvent event;

    @Column(name = "tx_hash")
    private String txHash;

    @Column(name = "block_number")
    private Long blockNumber;

    @Column(name = "status")
    private String status;

    @Column(name = "confirmed_at")
    private Long confirmedAt;

    @Override
    public String toString() {
        return "LedgerRecord{id=" + getId() + ", txHash='" + txHash + "', status='" + status + "'}";
    }
}
