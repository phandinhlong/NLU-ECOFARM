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

    public PublicChainStatus() {}

    // JPQL constructor expression cho module VER (khong can snapshotId/confirmations)
    public PublicChainStatus(Long id, Integer chainId, String contractAddress, String txHash,
                             Long blockNumber, String status, Long anchoredAt, String explorerUrl) {
        this.id = id;
        this.chainId = chainId;
        this.contractAddress = contractAddress;
        this.txHash = txHash;
        this.blockNumber = blockNumber;
        this.status = status;
        this.anchoredAt = anchoredAt;
        this.explorerUrl = explorerUrl;
    }

}
