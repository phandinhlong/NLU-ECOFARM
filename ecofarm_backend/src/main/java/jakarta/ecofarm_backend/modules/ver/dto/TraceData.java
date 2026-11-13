package jakarta.ecofarm_backend.modules.ver.dto;

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
public class TraceData {

    private String code;
    private Integer serial;
    private String qrStatus;
    private String packageCode;

    private Long batchId;
    private String batchCode;
    private String batchStatus;
    private String sowDate;
    private String harvestDate;
    private Long activatedAt;
    private Long saltEnc;

    private Long greenhouseId;
    private String greenhouseCode;
    private String greenhouseName;

    private String varietyCode;
    private String varietyName;

    private List<CertificateInfo> certificates;
    private List<EventData> timeline;
    private QcMetricInfo metrics;

    private String merkleRoot;
    private Integer leafCount;
    private String leavesJson;
    private List<AnchorInfo> anchors;
    private List<RecallInfo> recalls;
    private Boolean batchActive;
    private Boolean recalled;
}
