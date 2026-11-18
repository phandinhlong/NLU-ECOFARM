package jakarta.ecofarm_backend.common.dto.response;

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
public class ShipmentEligibleBatchResponse {

    private Long id;
    private String batchCode;
    private String greenhouseCode;
    private String greenhouseName;
    private String varietyName;
    private String status;
}
