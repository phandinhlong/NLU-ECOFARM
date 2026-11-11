package jakarta.ecofarm_backend.common.dto;

public class BlockchainStatus {
    boolean anchored;
    String chainName;
    String merkleRoot;
    String txHash;
    Long anchoredAt;
    String explorerUrl;

    public BlockchainStatus() {}

    public BlockchainStatus(boolean anchored, String chain, String mr, String tx, Long att, String exp) {
        this.anchored = anchored;
        this.chainName = chain;
        this.merkleRoot = mr;
        this.txHash = tx;
        this.anchoredAt = att;
        this.explorerUrl = exp;
    }

    public boolean isAnchored() { return anchored; }
    public void setAnchored(boolean anchored) { this.anchored = anchored; }
    public String getChainName() { return chainName; }
    public void setChainName(String chainName) { this.chainName = chainName; }
    public String getMerkleRoot() { return merkleRoot; }
    public void setMerkleRoot(String merkleRoot) { this.merkleRoot = merkleRoot; }
    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash; }
    public Long getAnchoredAt() { return anchoredAt; }
    public void setAnchoredAt(Long anchoredAt) { this.anchoredAt = anchoredAt; }
    public String getExplorerUrl() { return explorerUrl; }
    public void setExplorerUrl(String explorerUrl) { this.explorerUrl = explorerUrl; }
}
