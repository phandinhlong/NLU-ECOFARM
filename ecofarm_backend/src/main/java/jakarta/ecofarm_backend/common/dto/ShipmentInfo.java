package jakarta.ecofarm_backend.common.dto;

public class ShipmentInfo {
    String destination;
    Long shippedAt;

    public ShipmentInfo() {}

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public Long getShippedAt() { return shippedAt; }
    public void setShippedAt(Long shippedAt) { this.shippedAt = shippedAt; }
}
