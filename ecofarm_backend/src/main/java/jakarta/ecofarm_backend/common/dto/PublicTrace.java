package jakarta.ecofarm_backend.common.dto;

import java.util.List;

public class PublicTrace {
    String code;
    BatchInfo batch;
    TraceMetrics metrics;
    List<TimelineEvent> timeline;
    List<CertificateInfo> certificates;
    ShipmentInfo shipment;
    BlockchainStatus blockchain;
    RecallInfo recall;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public BatchInfo getBatch() { return batch; }
    public void setBatch(BatchInfo batch) { this.batch = batch; }
    public TraceMetrics getMetrics() { return metrics; }
    public void setMetrics(TraceMetrics metrics) { this.metrics = metrics; }
    public List<TimelineEvent> getTimeline() { return timeline; }
    public void setTimeline(List<TimelineEvent> timeline) { this.timeline = timeline; }
    public List<CertificateInfo> getCertificates() { return certificates; }
    public void setCertificates(List<CertificateInfo> certificates) { this.certificates = certificates; }
    public ShipmentInfo getShipment() { return shipment; }
    public void setShipment(ShipmentInfo shipment) { this.shipment = shipment; }
    public BlockchainStatus getBlockchain() { return blockchain; }
    public void setBlockchain(BlockchainStatus blockchain) { this.blockchain = blockchain; }
    public RecallInfo getRecall() { return recall; }
    public void setRecall(RecallInfo recall) { this.recall = recall; }
}
