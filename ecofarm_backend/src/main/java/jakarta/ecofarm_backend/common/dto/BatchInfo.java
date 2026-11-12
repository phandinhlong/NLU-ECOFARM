package jakarta.ecofarm_backend.common.dto;

public class BatchInfo {
    String batchCode;
    String status;
    String varietyName;
    String greenhouseCode;
    String greenhouseName;
    String sowDate;
    String harvestDate;

    public BatchInfo() {}

    public BatchInfo(String batchCode, String status, String vName, String gCode, String gName, String sDate, String hDate) {
        this.batchCode = batchCode;
        this.status = status;
        this.varietyName = vName;
        this.greenhouseCode = gCode;
        this.greenhouseName = gName;
        this.sowDate = sDate;
        this.harvestDate = hDate;
    }

    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getVarietyName() { return varietyName; }
    public void setVarietyName(String varietyName) { this.varietyName = varietyName; }
    public String getGreenhouseCode() { return greenhouseCode; }
    public void setGreenhouseCode(String greenhouseCode) { this.greenhouseCode = greenhouseCode; }
    public String getGreenhouseName() { return greenhouseName; }
    public void setGreenhouseName(String greenhouseName) { this.greenhouseName = greenhouseName; }
    public String getSowDate() { return sowDate; }
    public void setSowDate(String sowDate) { this.sowDate = sowDate; }
    public String getHarvestDate() { return harvestDate; }
    public void setHarvestDate(String harvestDate) { this.harvestDate = harvestDate; }
}
