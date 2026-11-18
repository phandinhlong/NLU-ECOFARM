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
