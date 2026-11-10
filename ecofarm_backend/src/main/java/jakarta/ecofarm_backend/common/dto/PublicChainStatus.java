package jakarta.ecofarm_backend.common.dto;

public class PublicChainStatus {
    Long id;
    Long snapshotId;
    Integer chainId;
    String contractAddress;
    String txHash;
    Long blockNumber;
    int confirmations;
    String status;
    String explorerUrl;
    Long anchoredAt;
    int requiredConfirmations;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSnapshotId() { return snapshotId; }
    public void setSnapshotId(Long snapshotId) { this.snapshotId = snapshotId; }
    public Integer getChainId() { return chainId; }
    public void setChainId(Integer chainId) { this.chainId = chainId; }
    public String getContractAddress() { return contractAddress; }
    public void setContractAddress(String contractAddress) { this.contractAddress = contractAddress; }
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
    public Long getAnchoredAt() { return anchoredAt; }
    public void setAnchoredAt(Long anchoredAt) { this.anchoredAt = anchoredAt; }
    public int getRequiredConfirmations() { return requiredConfirmations; }
    public void setRequiredConfirmations(int requiredConfirmations) { this.requiredConfirmations = requiredConfirmations; }
}
