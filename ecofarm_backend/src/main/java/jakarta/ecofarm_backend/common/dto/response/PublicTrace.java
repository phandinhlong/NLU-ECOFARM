package jakarta.ecofarm_backend.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
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
