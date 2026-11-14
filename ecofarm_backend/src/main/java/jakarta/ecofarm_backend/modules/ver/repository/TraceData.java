package jakarta.ecofarm_backend.modules.ver.repository;

import jakarta.ecofarm_backend.common.dto.response.CertificateInfo;
import jakarta.ecofarm_backend.common.dto.response.EventData;
import jakarta.ecofarm_backend.common.dto.response.PublicChainStatus;
import jakarta.ecofarm_backend.common.dto.response.QcMetricInfo;
import jakarta.ecofarm_backend.common.dto.response.RecallInfo;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
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
    private List<CultivationEvent> events;
    private QcMetricInfo metrics;

    private String merkleRoot;
    private Integer leafCount;
    private List<PublicChainStatus> anchors;
    private List<RecallInfo> recalls;
    private Boolean batchActive;
    private Boolean recalled;
}
