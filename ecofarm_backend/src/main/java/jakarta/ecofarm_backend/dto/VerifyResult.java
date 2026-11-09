package jakarta.ecofarm_backend.dto;

public class VerifyResult {
    String result;
    String computedRoot;
    String onChainRoot;
    String txHash;
    Long blockNumber;
    Long anchoredAt;
    boolean recalled;
    long checkedAt;

    public VerifyResult() {}

    public VerifyResult(String result, String cr, String ocr, String tx, Long bn, Long att, Boolean rec, long ct) {
        this.result = result;
        this.computedRoot = cr;
        this.onChainRoot = ocr;
        this.txHash = tx;
        this.blockNumber = bn;
        this.anchoredAt = att;
        this.recalled = rec;
        this.checkedAt = ct;
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
    public boolean isRecalled() { return recalled; }
    public void setRecalled(boolean recalled) { this.recalled = recalled; }
    public long getCheckedAt() { return checkedAt; }
    public void setCheckedAt(long checkedAt) { this.checkedAt = checkedAt; }
}
