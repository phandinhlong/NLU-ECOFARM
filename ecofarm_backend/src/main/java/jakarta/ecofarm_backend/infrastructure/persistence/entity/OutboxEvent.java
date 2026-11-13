package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "outbox_events", uniqueConstraints = @UniqueConstraint(columnNames = {"batch_id_hash", "seq"}))
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class OutboxEvent extends BaseEntity {

    @Column(name = "batch_id_hash", nullable = false)
    private String batchIdHash;

    @Column(nullable = false)
    private int seq;

    @Column(columnDefinition = "TEXT")
    private String eventData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    @Column(name = "attempts")
    private int attempts;

    @Column(name = "next_attempt_at")
    private Long nextAttemptAt;

    public enum OutboxStatus { PENDING, PROCESSING, SUCCESS, FAILED }

    @Override
    public String toString() {
        return "OutboxEvent{id=" + getId() + ", batchIdHash='" + batchIdHash + "', seq=" + seq + ", status=" + status + "}";
    }
}
