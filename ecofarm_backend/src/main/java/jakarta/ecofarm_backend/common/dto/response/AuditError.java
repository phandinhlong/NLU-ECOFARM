package jakarta.ecofarm_backend.common.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuditError {
    String message;
    String batchIdHash;
    int seq;

    public AuditError(String message, String batchIdHash, int seq) {
        this.message = message;
        this.batchIdHash = batchIdHash;
        this.seq = seq;
    }

    @Override
    public String toString() {
        return "AuditError{message='" + message + "', batchIdHash='" + batchIdHash + "', seq=" + seq + "}";
    }
}
