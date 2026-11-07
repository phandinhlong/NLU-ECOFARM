package jakarta.ecofarm_backend.persistence.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_id", nullable = true)
    private Long actorId;

    @Column(name = "actor_name", nullable = true)
    private String actorName;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String entity;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(columnDefinition = "JSONB")
    private Object detail;

    @Column(name = "ip_addr")
    private String ipAddr;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public AuditLog() {}

    public AuditLog(Long actorId, String actorName, String action, String entity, Long entityId, Object detail, String ipAddr) {
        this.actorId = actorId;
        this.actorName = actorName;
        this.action = action;
        this.entity = entity;
        this.entityId = entityId;
        this.detail = detail;
        this.ipAddr = ipAddr;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getActorId() { return actorId; }
    public void setActorId(Long actorId) { this.actorId = actorId; }
    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getEntity() { return entity; }
    public void setEntity(String entity) { this.entity = entity; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public Object getDetail() { return detail; }
    public void setDetail(Object detail) { this.detail = detail; }
    public String getIpAddr() { return ipAddr; }
    public void setIpAddr(String ipAddr) { this.ipAddr = ipAddr; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
