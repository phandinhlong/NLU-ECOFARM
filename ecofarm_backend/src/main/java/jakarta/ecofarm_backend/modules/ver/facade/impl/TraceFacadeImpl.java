package jakarta.ecofarm_backend.modules.ver.facade.impl;

import jakarta.ecofarm_backend.modules.ver.client.PolygonRpcClient;
import jakarta.ecofarm_backend.modules.ver.dto.AnchorInfo;
import jakarta.ecofarm_backend.modules.ver.dto.BatchInfo;
import jakarta.ecofarm_backend.modules.ver.dto.BlockchainStatusInfo;
import jakarta.ecofarm_backend.modules.ver.dto.ErrorResponse;
import jakarta.ecofarm_backend.modules.ver.dto.GreenhouseInfo;
import jakarta.ecofarm_backend.modules.ver.dto.ProofBundleResponse;
import jakarta.ecofarm_backend.modules.ver.dto.TraceData;
import jakarta.ecofarm_backend.modules.ver.dto.TraceResponse;
import jakarta.ecofarm_backend.modules.ver.dto.VarietyInfo;
import jakarta.ecofarm_backend.modules.ver.dto.VerVerifyResponse;
import jakarta.ecofarm_backend.modules.ver.facade.TraceFacade;
import jakarta.ecofarm_backend.modules.ver.repository.VerTraceRepository;
import jakarta.ecofarm_backend.modules.ver.util.MerkleUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class TraceFacadeImpl implements TraceFacade {

    private static final Logger log = LogManager.getLogger(TraceFacadeImpl.class);

    private static final String RESULT_MATCH = "MATCH";
    private static final String RESULT_MISMATCH = "MISMATCH";
    private static final String RESULT_UNAVAILABLE = "UNAVAILABLE";

    @Inject
    private VerTraceRepository traceRepository;

    @Inject
    private PolygonRpcClient polygonRpcClient;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.ver.chain-id", defaultValue = "80002")
    private Integer expectedChainId;

    @Override
    public TraceResponse getTrace(String code) {
        log.info("getTrace code={}", code);
        return mapToResponse(requireTrace(code));
    }

    @Override
    public VerVerifyResponse verifyTrace(String code) {
        return doVerify(code, requireTrace(code));
    }

    @Override
    public ProofBundleResponse getProofBundle(String code) {
        TraceData data = requireTrace(code);
        TraceResponse trace = mapToResponse(data);
        return ProofBundleResponse.builder()
                .code(trace.getCode())
                .batch(trace.getBatch())
                .variety(trace.getVariety())
                .greenhouse(trace.getGreenhouse())
                .certificates(trace.getCertificates())
                .metrics(trace.getMetrics())
                .timeline(trace.getTimeline())
                .blockchain(trace.getBlockchain())
                .recall(trace.getRecall())
                .leaves(MerkleUtil.parseLeaves(data.getLeavesJson()))
                .verification(doVerify(code, data))
                .build();
    }

    private TraceData requireTrace(String code) {
        TraceData data = traceRepository.load(code);
        if (data == null) {
            throw qrNotFound();
        }
        return data;
    }

    private VerVerifyResponse doVerify(String code, TraceData data) {
        long checkedAt = System.currentTimeMillis();

        AnchorInfo anchor = data.getAnchors() != null && !data.getAnchors().isEmpty()
                ? data.getAnchors().get(0)
                : null;

        VerVerifyResponse.VerVerifyResponseBuilder builder = VerVerifyResponse.builder()
                .code(code)
                .checkedAt(checkedAt)
                .recalled(Boolean.TRUE.equals(data.getRecalled()));

        if (anchor != null) {
            builder.txHash(anchor.getTxHash())
                    .blockNumber(anchor.getBlockNumber())
                    .anchoredAt(anchor.getAnchoredAt())
                    .explorerUrl(anchor.getExplorerUrl());
        }

        String storedRoot = data.getMerkleRoot();
        builder.storedRoot(storedRoot);
        if (storedRoot == null || data.getLeavesJson() == null) {
            return builder.result(RESULT_UNAVAILABLE)
                    .message("Chưa có snapshot Merkle cho mã này")
                    .build();
        }

        String computedRoot = MerkleUtil.computeRoot(MerkleUtil.parseLeaves(data.getLeavesJson()));
        builder.computedRoot(computedRoot);
        if (computedRoot == null) {
            return builder.result(RESULT_UNAVAILABLE)
                    .message("Snapshot không có leaves để dựng lại Merkle root")
                    .build();
        }

        if (!storedRoot.equalsIgnoreCase(computedRoot)) {
            return builder.result(RESULT_MISMATCH)
                    .message("Root trong DB khác root tính lại từ dữ liệu sự kiện — dữ liệu có thể đã bị chỉnh sửa")
                    .build();
        }

        if (anchor == null) {
            return builder.result(RESULT_UNAVAILABLE)
                    .message("Chưa có anchor trên blockchain cho snapshot này")
                    .build();
        }

        if (anchor.getChainId() == null || anchor.getChainId().intValue() != expectedChainId) {
            return builder.result(RESULT_UNAVAILABLE)
                    .message("Anchor nằm trên chain " + anchor.getChainId()
                            + ", không kiểm chứng được trên Polygon Amoy (chainId " + expectedChainId + ")")
                    .build();
        }

        String onChainRoot = polygonRpcClient.callRoot(anchor.getContractAddress());
        builder.onChainRoot(onChainRoot);
        if (onChainRoot == null) {
            return builder.result(RESULT_UNAVAILABLE)
                    .message("Không đọc được root trên Polygon Amoy, vui lòng thử lại sau")
                    .build();
        }

        if (onChainRoot.equalsIgnoreCase(computedRoot)) {
            return builder.result(RESULT_MATCH)
                    .message("Dữ liệu khớp với root trên Polygon Amoy — xác thực thành công")
                    .build();
        }
        return builder.result(RESULT_MISMATCH)
                .message("Root trên chain khác root tính lại — dữ liệu đã bị chỉnh sửa sau khi neo")
                .build();
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
        BlockchainStatusInfo.BlockchainStatusInfoBuilder builder = BlockchainStatusInfo.builder()
                .merkleRoot(data.getMerkleRoot())
                .leafCount(data.getLeafCount());

        AnchorInfo anchor = data.getAnchors() != null && !data.getAnchors().isEmpty()
                ? data.getAnchors().get(0)
                : null;
        if (anchor != null) {
            String explorerUrl = anchor.getExplorerUrl();
            if (explorerUrl == null && anchor.getTxHash() != null
                    && anchor.getChainId() != null && anchor.getChainId().intValue() == expectedChainId) {
                explorerUrl = "https://amoy.polygonscan.com/tx/" + anchor.getTxHash();
            }
            builder.chainId(anchor.getChainId())
                    .contractAddress(anchor.getContractAddress())
                    .txHash(anchor.getTxHash())
                    .blockNumber(anchor.getBlockNumber())
                    .anchoredAt(anchor.getAnchoredAt())
                    .explorerUrl(explorerUrl);
            if ("CONFIRMED".equalsIgnoreCase(anchor.getStatus())) {
                status = "VERIFIED";
            } else if ("PENDING".equalsIgnoreCase(anchor.getStatus())) {
                status = "PENDING";
            }
        }
        return builder.status(status).build();
    }
}
