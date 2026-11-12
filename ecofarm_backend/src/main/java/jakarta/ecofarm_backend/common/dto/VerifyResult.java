package jakarta.ecofarm_backend.common.dto;

public class VerifyResult {
    String result;
    String computedRoot;
    String onChainRoot;
    String txHash;
    Long blockNumber;
    Long anchoredAt;
    String explorerUrl;
    boolean recalled;
    String recallReason;
    long checkedAt;
    String message;

    public VerifyResult() {}

    public VerifyResult(String result, String cr, String ocr, String tx, Long bn, Long att,
                        String explorerUrl, Boolean rec, long ct, String message) {
        this.result = result;
        this.computedRoot = cr;
        this.onChainRoot = ocr;
        this.txHash = tx;
        this.blockNumber = bn;
        this.anchoredAt = att;
        this.explorerUrl = explorerUrl;
        this.recalled = rec;
        this.checkedAt = ct;
        this.message = message;
    }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getComputedRoot() { return computedRoot; }
    public void setComputedRoot(String computedRoot) { this.computedRoot = computedRoot; }
    public String getOnChainRoot() { return onChainRoot; }
    public void setOnChainRoot(String onChainRoot) { this.onChainRoot = onChainRoot; }
    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash; }
    public Long getBlockNumber() { return blockNumber; }
    public void setBlockNumber(Long blockNumber) { this.blockNumber = blockNumber; }
    public Long getAnchoredAt() { return anchoredAt; }
    public void setAnchoredAt(Long anchoredAt) { this.anchoredAt = anchoredAt; }
    public String getExplorerUrl() { return explorerUrl; }
    public void setExplorerUrl(String explorerUrl) { this.explorerUrl = explorerUrl; }
    public boolean isRecalled() { return recalled; }
    public void setRecalled(boolean recalled) { this.recalled = recalled; }
    public String getRecallReason() { return recallReason; }
    public void setRecallReason(String recallReason) { this.recallReason = recallReason; }
    public long getCheckedAt() { return checkedAt; }
    public void setCheckedAt(long checkedAt) { this.checkedAt = checkedAt; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
