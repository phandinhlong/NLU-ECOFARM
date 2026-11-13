package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "shipments")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Shipment extends BaseEntity {

    // JPA relation thay cho DB foreign key (batches.id)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "batch_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    private Batch batch;

    @Column(name = "destination", nullable = false)
    private String destination;

    @Column(name = "vehicle_plate", nullable = false, length = 20)
    private String vehiclePlate;

    @Column(name = "shipped_at")
    private Long shippedAt;

    @Column(name = "received_at")
    private Long receivedAt;

    @Column(name = "status")
    private String status;

    @Override
    public String toString() {
        return "Shipment{id=" + getId() + ", destination='" + destination + "', status='" + status + "'}";
    }
}
