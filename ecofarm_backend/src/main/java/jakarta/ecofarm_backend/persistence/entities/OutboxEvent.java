package jakarta.ecofarm_backend.persistence.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "outbox_events", uniqueConstraints = @UniqueConstraint(columnNames = {"batch_id_hash", "seq"}))
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    private Instant nextAttemptAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum OutboxStatus { PENDING, PROCESSING, SUCCESS, FAILED }

    public OutboxEvent() {}

    public OutboxEvent(String batchIdHash, int seq, String eventData) {
        this.batchIdHash = batchIdHash;
        this.seq = seq;
        this.eventData = eventData;
        this.status = OutboxStatus.PENDING;
        this.attempts = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBatchIdHash() { return batchIdHash; }
    public void setBatchIdHash(String batchIdHash) { this.batchIdHash = batchIdHash; }
    public int getSeq() { return seq; }
    public void setSeq(int seq) { this.seq = seq; }
    public String getEventData() { return eventData; }
    public void setEventData(String eventData) { this.eventData = eventData; }
    public OutboxStatus getStatus() { return status; }
    public void setStatus(OutboxStatus status) { this.status = status; }
    public int getAttemps() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public InstantgetNextAttemptAt() { return nextAttemptAt; }
    public void setNextAttemptAt(Instant nextAttemptAt) { this.nextAttemptAt = nextAttemptAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
