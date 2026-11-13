package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cultivation_events")
@Getter
@Setter
@NoArgsConstructor
public class CultivationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "client_event_id", unique = true)
    private String clientEventId;

    @Column(name = "amends_event_id")
    private Long amendsEventId;

    @Column(name = "is_active")
    private Boolean active;

    @Version
    @Column(name = "version")
    private Integer version;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;
}
