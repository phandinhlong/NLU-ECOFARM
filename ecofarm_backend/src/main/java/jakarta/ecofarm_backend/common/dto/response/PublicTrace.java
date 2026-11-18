package jakarta.ecofarm_backend.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicTrace {
    String code;
    BatchInfo batch;
    VarietyInfo variety;
    GreenhouseInfo greenhouse;
    List<CertificateInfo> certificates;
    QcMetricInfo metrics;
    List<EventData> timeline;
    BlockchainStatus blockchain;
    RecallInfo recall;
    ScanInfo scanInfo;
}
