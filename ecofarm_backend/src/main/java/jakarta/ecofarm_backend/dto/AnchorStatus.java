package jakarta.ecofarm_backend.dto;

public class AnchorStatus {
    Long batchId;
    String batchStatus;
    PrivateChainStatus privateChain;
    PublicChainStatus publicChain;
    Integer attempts;
    String lastError;
    Long nextAttemptAt;

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getBatchStatus() { return batchStatus; }
    public void setBatchStatus(String batchStatus) { this.batchStatus = batchStatus; }
    public PrivateChainStatus getPrivateChain() { return privateChain; }
    public void setPrivateChain(PrivateChainStatus privateChain) { this.privateChain = privateChain; }
    public PublicChainStatus getPublicChain() { return publicChain; }
    public void setPublicChain(PublicChainStatus publicChain) { this.publicChain = publicChain; }
    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
    public Long getNextAttemptAt() { return nextAttemptAt; }
    public void setNextAttemptAt(Long nextAttemptAt) { this.nextAttemptAt = nextAttemptAt; }
}
