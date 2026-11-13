package jakarta.ecofarm_backend.modules.ver.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BlockchainStatusInfo {

    private String status;
    private String merkleRoot;
    private Integer leafCount;
    private Integer chainId;
    private String contractAddress;
    private String txHash;
    private Long blockNumber;
    private Long anchoredAt;
    private String explorerUrl;
}
