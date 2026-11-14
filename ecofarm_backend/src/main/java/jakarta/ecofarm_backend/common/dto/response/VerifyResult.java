package jakarta.ecofarm_backend.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class VerifyResult {
    String code;
    String result;
    String storedRoot;
    String computedRoot;
    String onChainRoot;
    String txHash;
    Long blockNumber;
    Long anchoredAt;
    String explorerUrl;
    boolean recalled;
    long checkedAt;
    String message;

    public VerifyResult() {}

    public VerifyResult(String result, String cr, String ocr, String tx, Long bn, Long att,
                        String explorerUrl, Boolean rec, long ct, String message) {
        this.result = result;
        this.computedRoot = cr;
        this.onChainRoot = ocr;
        this.txHash = tx;
        this.blockNumber = bn;
        this.anchoredAt = att;
        this.explorerUrl = explorerUrl;
        this.recalled = rec;
        this.checkedAt = ct;
        this.message = message;
    }

}
