package jakarta.ecofarm_backend.common.dto;

public class PrivateChainStatus {
    boolean finalized;
    String txHash;

    public PrivateChainStatus(boolean finalized, String txHash) {
        this.finalized = finalized;
        this.txHash = txHash;
    }

    public boolean isFinalized() { return finalized; }
    public void setFinalized(boolean finalized) { this.finalized = finalized; }
    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash; }
}
