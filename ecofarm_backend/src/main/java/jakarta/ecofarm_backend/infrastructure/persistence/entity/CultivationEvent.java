package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.ecofarm_backend.common.enums.AmendReason;
import jakarta.ecofarm_backend.common.enums.EventChainStatus;
import jakarta.ecofarm_backend.common.enums.EventReviewStatus;
import jakarta.ecofarm_backend.common.enums.EventType;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private EventType eventType;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "event_hash")
    private String eventHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EventChainStatus status;

    @Column(name = "occurred_at")
    private Long occurredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status")
    @Builder.Default
    private EventReviewStatus reviewStatus = EventReviewStatus.UNREVIEWED;

    // JPA relation thay cho DB foreign key (users.id)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "reviewed_by", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private Long reviewedAt;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "pending_approver_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    private User pendingApprover;

    @Enumerated(EnumType.STRING)
    @Column(name = "amend_reason")
    private AmendReason amendReason;

    @Column(name = "amend_note", columnDefinition = "TEXT")
    private String amendNote;

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

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    @Builder.Default
    private List<EventAttachment> eventAttachments = new ArrayList<>();

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    @Builder.Default
    private List<LedgerRecord> ledgerRecords = new ArrayList<>();
}
