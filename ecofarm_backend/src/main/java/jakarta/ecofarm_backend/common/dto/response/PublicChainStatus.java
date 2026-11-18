package jakarta.ecofarm_backend.common.dto.response;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
    @Positive
    Long id;
    @Positive
    Long snapshotId;
    @Positive
    Integer chainId;
    String contractAddress;
    String txHash;
    @Positive
    Long blockNumber;
    @PositiveOrZero
    int confirmations;
    String status;
    String explorerUrl;
    @Positive
    Long anchoredAt;
    @Positive
    int requiredConfirmations;
}
