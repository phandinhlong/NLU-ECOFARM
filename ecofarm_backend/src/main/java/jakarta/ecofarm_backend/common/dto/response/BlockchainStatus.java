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
public class BlockchainStatus {
    boolean anchored;
    String chainName;
    String merkleRoot;
    String txHash;
    Long anchoredAt;
    String explorerUrl;
    String status;
    Integer leafCount;
    Integer chainId;
    String contractAddress;
    Long blockNumber;

    public BlockchainStatus(boolean anchored, String chain, String mr, String tx, Long att, String exp) {
        this.anchored = anchored;
        this.chainName = chain;
        this.merkleRoot = mr;
        this.txHash = tx;
        this.anchoredAt = att;
        this.explorerUrl = exp;
    }
}
