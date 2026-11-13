package jakarta.ecofarm_backend.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer leafCount;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer chainId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String contractAddress;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Long blockNumber;

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
