package jakarta.ecofarm_backend.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditError {
    String message;
    String batchIdHash;
    int seq;

    @Override
    public String toString() {
        return "AuditError{message='" + message + "', batchIdHash='" + batchIdHash + "', seq=" + seq + "}";
    }
}
