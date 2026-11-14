package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cultivation_events")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class CultivationEvent extends BaseEntity {

    // JPA relation thay cho DB foreign key (batches.id)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "batch_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    private Batch batch;

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

    // JPA relation thay cho DB foreign key (users.id)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "recorded_by", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    private User recordedBy;

    @Column(name = "recorded_at")
    private Long recordedAt;

    @Column(name = "client_event_id", unique = true)
    private String clientEventId;

    // JPA self relation thay cho DB foreign key (cultivation_events.id)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "amends_event_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    private CultivationEvent amendsEvent;
    @OneToMany(mappedBy = "amendsEvent", fetch = FetchType.LAZY)
    @Builder.Default
    private List<CultivationEvent> amendedEvents = new ArrayList<>();
    @Builder.Default
    private List<EventAttachment> eventAttachments = new ArrayList<>();
    @Builder.Default
    private List<LedgerRecord> ledgerRecords = new ArrayList<>();
}
