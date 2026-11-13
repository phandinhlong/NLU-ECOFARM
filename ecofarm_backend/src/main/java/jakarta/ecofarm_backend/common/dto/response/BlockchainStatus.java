package jakarta.ecofarm_backend.common.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
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

}
