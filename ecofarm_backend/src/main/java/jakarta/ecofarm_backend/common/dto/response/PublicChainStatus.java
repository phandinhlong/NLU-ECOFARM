package jakarta.ecofarm_backend.common.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PublicChainStatus {
    Long id;
    Long snapshotId;
    Integer chainId;
    String contractAddress;
    String txHash;
    Long blockNumber;
    int confirmations;
    String status;
    String explorerUrl;
    Long anchoredAt;
    int requiredConfirmations;

}
