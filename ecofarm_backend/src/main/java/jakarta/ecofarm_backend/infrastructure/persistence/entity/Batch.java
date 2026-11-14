package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.inject.Inject;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "batches")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Batch extends BaseEntity {

    @Column(name = "batch_code", unique = true)
    private String batchCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "greenhouse_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    private Greenhouse greenhouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "variety_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    private Variety variety;

    @Column(name = "status")
    private String status;

    @Column(name = "sow_date")
    private LocalDate sowDate;

    @Column(name = "harvest_date")
    private LocalDate harvestDate;

    @Column(name = "salt_enc")
    private Long saltEnc;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "created_by",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    private User createdBy;

    @Column(name = "activated_at")
    private Long activatedAt;
    @Builder.Default
    List<BatchAssignment> batchAssignments=new ArrayList<>();
    @Builder.Default
    private List<CultivationEvent> cultivationEvents = new ArrayList<>();
    @Builder.Default
    private List<PackageEntity> packageEntities = new ArrayList<>();
    @Builder.Default
    private List<QcRecord> qcRecords = new ArrayList<>();
    @Builder.Default
    private List<Recall> recalls = new ArrayList<>();
    @Builder.Default
    private List<Shipment> shipments = new ArrayList<>();
    @Override
    public String toString() {
        return "Batch{id=" + getId() + ", batchCode='" + batchCode + "', status='" + status + "'}";
    }
}
