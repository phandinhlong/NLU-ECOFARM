package jakarta.ecofarm_backend.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "shipments")
public class Shipment {

    @Id
    private Long id;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "destination")
    private String destination;

    @Column(name = "vehicle_plate")
    private String vehiclePlate;

    @Column(name = "shipped_at")
    private Long shippedAt;

    @Column(name = "received_at")
    private Long receivedAt;

    @Column(name = "status")
    private String status;

    public Shipment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getVehiclePlate() { return vehiclePlate; }
    public void setVehiclePlate(String vehiclePlate) { this.vehiclePlate = vehiclePlate; }
    public Long getShippedAt() { return shippedAt; }
    public void setShippedAt(Long shippedAt) { this.shippedAt = shippedAt; }
    public Long getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Long receivedAt) { this.receivedAt = receivedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
