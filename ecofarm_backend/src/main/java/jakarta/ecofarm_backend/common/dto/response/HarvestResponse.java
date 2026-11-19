package jakarta.ecofarm_backend.common.dto.response;

import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HarvestResponse {
    @Positive
    private Long batchId;
    private String batchCode;
    private String status;
    private String harvestDate;
    @Positive
    private Integer estimatedQuantity;
    private String area;
    private String eventHash;
    private String besuTxHash;
    @Positive
    private Long besuBlockNumber;
    private String explorerUrl;
    private String message;
}
