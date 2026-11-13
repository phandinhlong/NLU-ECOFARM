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
public class VerVerifyResponse {

    private String code;
    private String result;
    private String storedRoot;
    private String computedRoot;
    private String onChainRoot;
    private String txHash;
    private Long blockNumber;
    private Long anchoredAt;
    private String explorerUrl;
    private Long checkedAt;
    private String message;
    private Boolean recalled;
}
