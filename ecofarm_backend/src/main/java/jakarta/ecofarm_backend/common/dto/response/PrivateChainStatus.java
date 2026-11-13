package jakarta.ecofarm_backend.common.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PrivateChainStatus {
    boolean finalized;
    String txHash;

    public PrivateChainStatus(boolean finalized, String txHash) {
        this.finalized = finalized;
        this.txHash = txHash;
    }

}
