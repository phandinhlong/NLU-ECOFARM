package jakarta.ecofarm_backend.dto;

public class ReconciliationItem {
    Long batchId;
    String batchCode;
    String dbRoot;
    String privateRoot;
    String publicRoot;
    String result;

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }
    public String getDbRoot() { return dbRoot; }
    public void setDbRoot(String dbRoot) { this.dbRoot = dbRoot; }
    public String getPrivateRoot() { return privateRoot; }
    public void setPrivateRoot(String privateRoot) { this.privateRoot = privateRoot; }
    public String getPublicRoot() { return publicRoot; }
    public void setPublicRoot(String publicRoot) { this.publicRoot = publicRoot; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}
