package jakarta.ecofarm_backend.dto;

public class PublicChainStatus {
    String txHash;
    Long blockNumber;
    int confirmations;
    String status;
    String explorerUrl;

    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash; }
    public Long getBlockNumber() { return blockNumber; }
    public void setBlockNumber(Long blockNumber) { this.blockNumber = blockNumber; }
    public int getConfirmations() { return confirmations; }
    public void setConfirmations(int confirmations) { this.confirmations = confirmations; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getExplorerUrl() { return explorerUrl; }
    public void setExplorerUrl(String explorerUrl) { this.explorerUrl = explorerUrl; }
}
