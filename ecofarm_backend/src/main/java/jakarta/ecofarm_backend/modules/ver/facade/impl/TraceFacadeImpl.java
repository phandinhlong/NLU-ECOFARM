package jakarta.ecofarm_backend.modules.ver.facade.impl;

import jakarta.ecofarm_backend.modules.ver.dto.AnchorInfo;
import jakarta.ecofarm_backend.modules.ver.dto.BatchInfo;
import jakarta.ecofarm_backend.modules.ver.dto.BlockchainStatusInfo;
import jakarta.ecofarm_backend.modules.ver.dto.ErrorResponse;
import jakarta.ecofarm_backend.modules.ver.dto.GreenhouseInfo;
import jakarta.ecofarm_backend.modules.ver.dto.TraceData;
import jakarta.ecofarm_backend.modules.ver.dto.TraceResponse;
import jakarta.ecofarm_backend.modules.ver.dto.VarietyInfo;
import jakarta.ecofarm_backend.modules.ver.facade.TraceFacade;
import jakarta.ecofarm_backend.modules.ver.repository.VerTraceRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ApplicationScoped
public class TraceFacadeImpl implements TraceFacade {

    private static final Logger log = LogManager.getLogger(TraceFacadeImpl.class);

    @Inject
    private VerTraceRepository traceRepository;

    @Override
    public TraceResponse getTrace(String code) {
        log.info("getTrace code={}", code);
        TraceData data = traceRepository.load(code);
        if (data == null) {
            throw qrNotFound();
        }
        return mapToResponse(data);
    }

    private WebApplicationException qrNotFound() {
        return new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(ErrorResponse.builder()
                        .code("QR_NOT_FOUND")
                        .message("Không tìm thấy mã truy xuất")
                        .isSuccess(false)
                        .build())
                .build());
    }

    private TraceResponse mapToResponse(TraceData data) {
        return TraceResponse.builder()
                .code(data.getCode())
                .batch(data.getBatchCode() != null
                        ? BatchInfo.builder()
                                .batchCode(data.getBatchCode())
                                .status(data.getBatchStatus())
                                .sowDate(data.getSowDate())
                                .harvestDate(data.getHarvestDate())
                                .activatedAt(data.getActivatedAt())
                                .active(data.getBatchActive())
                                .recalled(data.getRecalled())
                                .build()
                        : null)
                .variety(data.getVarietyCode() != null
                        ? VarietyInfo.builder()
                                .code(data.getVarietyCode())
                                .name(data.getVarietyName())
                                .build()
                        : null)
                .greenhouse(data.getGreenhouseCode() != null
                        ? GreenhouseInfo.builder()
                                .code(data.getGreenhouseCode())
                                .name(data.getGreenhouseName())
                                .build()
                        : null)
                .certificates(data.getCertificates())
                .metrics(data.getMetrics())
                .timeline(data.getTimeline())
                .blockchain(data.getMerkleRoot() != null ? mapBlockchain(data) : null)
                .recall(data.getRecalls() != null && !data.getRecalls().isEmpty()
                        ? data.getRecalls().get(0)
                        : null)
                .build();
    }

    private BlockchainStatusInfo mapBlockchain(TraceData data) {
        String status = "UNVERIFIED";
        if (data.getAnchors() != null && !data.getAnchors().isEmpty()) {
            AnchorInfo latest = data.getAnchors().get(0);
            if ("CONFIRMED".equalsIgnoreCase(latest.getStatus())) {
                status = "VERIFIED";
            } else if ("PENDING".equalsIgnoreCase(latest.getStatus())) {
                status = "PENDING";
            }
        }
        return BlockchainStatusInfo.builder()
                .status(status)
                .merkleRoot(data.getMerkleRoot())
                .leafCount(data.getLeafCount())
                .build();
    }
}
