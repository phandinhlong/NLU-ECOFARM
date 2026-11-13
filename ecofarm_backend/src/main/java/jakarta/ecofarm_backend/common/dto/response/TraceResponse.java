package jakarta.ecofarm_backend.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TraceResponse {

    private String code;
    private TraceBatchInfo batch;
    private VarietyInfo variety;
    private GreenhouseInfo greenhouse;
    private List<CertificateInfo> certificates;
    private QcMetricInfo metrics;
    private List<EventData> timeline;
    private BlockchainStatusInfo blockchain;
    private RecallInfo recall;
}
