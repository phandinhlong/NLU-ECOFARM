package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;

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
    private Long nextAttemptAt;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    public enum OutboxStatus { PENDING, PROCESSING, SUCCESS, FAILED }

    public OutboxEvent() {}

    public OutboxEvent(String batchIdHash, int seq, String eventData) {
        this.batchIdHash = batchIdHash;
        this.seq = seq;
        this.eventData = eventData;
        this.status = OutboxStatus.PENDING;
        this.attempts = 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
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
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public Long getNextAttemptAt() { return nextAttemptAt; }
    public void setNextAttemptAt(Long nextAttemptAt) { this.nextAttemptAt = nextAttemptAt; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
}
