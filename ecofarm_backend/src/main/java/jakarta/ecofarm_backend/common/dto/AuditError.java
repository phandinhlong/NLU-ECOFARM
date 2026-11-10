package jakarta.ecofarm_backend.common.dto;

public class AuditError {
    String message;
    String batchIdHash;
    int seq;

    public AuditError(String message, String batchIdHash, int seq) {
        this.message = message;
        this.batchIdHash = batchIdHash;
        this.seq = seq;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getBatchIdHash() { return batchIdHash; }
    public void setBatchIdHash(String batchIdHash) { this.batchIdHash = batchIdHash; }
    public int getSeq() { return seq; }
    public void setSeq(int seq) { this.seq = seq; }

    @Override
    public String toString() {
        return "AuditError{message='" + message + "', batchIdHash='" + batchIdHash + "', seq=" + seq + "}";
    }
}
