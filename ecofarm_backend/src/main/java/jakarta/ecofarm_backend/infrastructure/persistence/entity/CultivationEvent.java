package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cultivation_events")
public class CultivationEvent {

    @Id
    private Long id;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "seq")
    private Integer seq;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "payload")
    private Long payload;

    @Column(name = "event_hash")
    private String eventHash;

    @Column(name = "status")
    private String status;

    @Column(name = "recorded_by")
    private Long recordedBy;

    @Column(name = "recorded_at")
    private Long recordedAt;

    @Column(name = "client_event_id")
    private String clientEventId;

    @Column(name = "amends_event_id")
    private Long amendsEventId;

    public CultivationEvent() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Integer getSeq() { return seq; }
    public void setSeq(Integer seq) { this.seq = seq; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Long getPayload() { return payload; }
    public void setPayload(Long payload) { this.payload = payload; }
    public String getEventHash() { return eventHash; }
    public void setEventHash(String eventHash) { this.eventHash = eventHash; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getRecordedBy() { return recordedBy; }
    public void setRecordedBy(Long recordedBy) { this.recordedBy = recordedBy; }
    public Long getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Long recordedAt) { this.recordedAt = recordedAt; }
    public String getClientEventId() { return clientEventId; }
    public void setClientEventId(String clientEventId) { this.clientEventId = clientEventId; }
    public Long getAmendsEventId() { return amendsEventId; }
    public void setAmendsEventId(Long amendsEventId) { this.amendsEventId = amendsEventId; }
}
