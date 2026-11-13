package jakarta.ecofarm_backend.modules.ver.facade.impl;

import jakarta.ecofarm_backend.common.dto.response.BatchInfo;
import jakarta.ecofarm_backend.common.dto.response.BlockchainStatus;
import jakarta.ecofarm_backend.common.dto.response.EventData;
import jakarta.ecofarm_backend.common.dto.response.ExceptionResponse;
import jakarta.ecofarm_backend.common.dto.response.GreenhouseInfo;
import jakarta.ecofarm_backend.common.dto.response.ProofBundle;
import jakarta.ecofarm_backend.common.dto.response.ProofBundleResponse;
import jakarta.ecofarm_backend.common.dto.response.ProofEvent;
import jakarta.ecofarm_backend.common.dto.response.ProofLeaf;
import jakarta.ecofarm_backend.common.dto.response.PublicChainStatus;
import jakarta.ecofarm_backend.common.dto.response.PublicTrace;
import jakarta.ecofarm_backend.common.dto.response.VarietyInfo;
import jakarta.ecofarm_backend.common.dto.response.VerifyResult;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import jakarta.ecofarm_backend.modules.ver.cache.AnchorRootCache;
import jakarta.ecofarm_backend.modules.ver.client.ChainRootReader;
import jakarta.ecofarm_backend.modules.ver.facade.TraceFacade;
import jakarta.ecofarm_backend.modules.ver.repository.TraceData;
import jakarta.ecofarm_backend.modules.ver.repository.VerTraceRepository;
import jakarta.ecofarm_backend.modules.ver.util.MerkleUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TraceFacadeImpl implements TraceFacade {

    private static final Logger log = LogManager.getLogger(TraceFacadeImpl.class);

    private static final String RESULT_MATCH = "MATCH";
    private static final String RESULT_MISMATCH = "MISMATCH";
    private static final String RESULT_UNAVAILABLE = "UNAVAILABLE";

    @Inject
    private VerTraceRepository traceRepository;

    @Inject
    private ChainRootReader chainRootReader;

    @Inject
    private AnchorRootCache anchorRootCache;

    @Override
    public PublicTrace getTrace(String code) {
        log.info("getTrace code={}", code);
        return mapToResponse(requireTrace(code));
    }

    @Override
    public VerifyResult verifyTrace(String code) {
        return doVerify(code, requireTrace(code));
    }

    @Override
    public ProofBundleResponse getProofBundle(String code) {
        TraceData data = requireTrace(code);
        return ProofBundleResponse.builder()
                .code(data.getCode())
                .trace(mapToResponse(data))
                .proof(mapProof(data))
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

    private VerifyResult doVerify(String code, TraceData data) {
        long checkedAt = System.currentTimeMillis();

        PublicChainStatus anchor = data.getAnchors() != null && !data.getAnchors().isEmpty()
                ? data.getAnchors().get(0)
                : null;

        VerifyResult.VerifyResultBuilder builder = VerifyResult.builder()
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

        Integer chainId = anchor.getChainId();
        String rpcEndpoint = chainRootReader.resolveEndpoint(chainId);
        if (rpcEndpoint == null) {
            return builder.result(RESULT_UNAVAILABLE)
                    .message("Chain " + chainId + " chưa được hỗ trợ xác thực (Polygon Amoy và private chain Besu)")
                    .build();
        }

        // Cache root tren chain de RPC sap khong chan luong xac thuc
        String cacheKey = chainId + ":" + anchor.getContractAddress();
        String onChainRoot = anchorRootCache.get(cacheKey);
        if (onChainRoot == null) {
            onChainRoot = chainRootReader.callRoot(rpcEndpoint, anchor.getContractAddress());
            if (onChainRoot != null) {
                anchorRootCache.put(cacheKey, onChainRoot);
            }
        }
        builder.onChainRoot(onChainRoot);
        if (onChainRoot == null) {
            return builder.result(RESULT_UNAVAILABLE)
                    .message("Không đọc được root trên " + chainRootReader.chainName(chainId) + ", vui lòng thử lại sau")
                    .build();
        }

        if (onChainRoot.equalsIgnoreCase(computedRoot)) {
            return builder.result(RESULT_MATCH)
                    .message("Dữ liệu khớp với root trên " + chainRootReader.chainName(chainId) + " — xác thực thành công")
                    .build();
        }
        return builder.result(RESULT_MISMATCH)
                .message("Root trên chain khác root tính lại — dữ liệu đã bị chỉnh sửa sau khi neo")
                .build();
    }

    private WebApplicationException qrNotFound() {
        return new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(ExceptionResponse.builder()
                        .code(Response.Status.NOT_FOUND.getStatusCode())
                        .errorCode("QR_NOT_FOUND")
                        .message("Không tìm thấy mã truy xuất")
                        .isSuccess(false)
                        .build())
                .build());
    }

    private PublicTrace mapToResponse(TraceData data) {
        PublicTrace trace = new PublicTrace();
        trace.setCode(data.getCode());

        if (data.getBatchCode() != null) {
            BatchInfo batch = new BatchInfo(
                    data.getBatchCode(),
                    data.getBatchStatus(),
                    data.getVarietyName(),
                    data.getGreenhouseCode(),
                    data.getGreenhouseName(),
                    data.getSowDate());
            batch.setHarvestDate(data.getHarvestDate());
            batch.setActivatedAt(data.getActivatedAt());
            batch.setActive(data.getBatchActive());
            batch.setRecalled(data.getRecalled());
            trace.setBatch(batch);
        }

        if (data.getVarietyCode() != null) {
            trace.setVariety(VarietyInfo.builder()
                    .code(data.getVarietyCode())
                    .name(data.getVarietyName())
                    .build());
        }
        if (data.getGreenhouseCode() != null) {
            trace.setGreenhouse(GreenhouseInfo.builder()
                    .code(data.getGreenhouseCode())
                    .name(data.getGreenhouseName())
                    .build());
        }

        trace.setCertificates(data.getCertificates());
        trace.setMetrics(data.getMetrics());
        trace.setTimeline(data.getTimeline());
        if (data.getMerkleRoot() != null) {
            trace.setBlockchain(mapBlockchain(data));
        }
        if (data.getRecalls() != null && !data.getRecalls().isEmpty()) {
            trace.setRecall(data.getRecalls().get(0));
        }
        return trace;
    }

    private BlockchainStatus mapBlockchain(TraceData data) {
        PublicChainStatus anchor = data.getAnchors() != null && !data.getAnchors().isEmpty()
                ? data.getAnchors().get(0)
                : null;

        String status = "UNVERIFIED";
        String chainName = null;
        String txHash = null;
        Long anchoredAt = null;
        String explorerUrl = null;

        if (anchor != null) {
            chainName = chainRootReader.chainName(anchor.getChainId());
            txHash = anchor.getTxHash();
            anchoredAt = anchor.getAnchoredAt();
            explorerUrl = anchor.getExplorerUrl();
            if (explorerUrl == null && anchor.getTxHash() != null
                    && chainRootReader.isPublicChain(anchor.getChainId())) {
                explorerUrl = "https://amoy.polygonscan.com/tx/" + anchor.getTxHash();
            }
            if ("CONFIRMED".equalsIgnoreCase(anchor.getStatus())) {
                status = "VERIFIED";
            } else if ("PENDING".equalsIgnoreCase(anchor.getStatus())) {
                status = "PENDING";
            }
        }

        BlockchainStatus blockchain = new BlockchainStatus(
                "VERIFIED".equals(status),
                chainName,
                data.getMerkleRoot(),
                txHash,
                anchoredAt,
                explorerUrl);
        blockchain.setStatus(status);
        blockchain.setLeafCount(data.getLeafCount());
        if (anchor != null) {
            blockchain.setChainId(anchor.getChainId());
            blockchain.setContractAddress(anchor.getContractAddress());
            blockchain.setBlockNumber(anchor.getBlockNumber());
        }
        return blockchain;
    }

    private ProofBundle mapProof(TraceData data) {
        ProofBundle proof = new ProofBundle();
        proof.setBatchCode(data.getBatchCode());
        proof.setSalt(data.getSaltEnc() != null ? String.valueOf(data.getSaltEnc()) : null);
        proof.setHashAlgorithm("Keccak-256");
        proof.setMerkleRoot(data.getMerkleRoot());
        proof.setGeneratedAt(System.currentTimeMillis());

        List<ProofLeaf> leaves = new ArrayList<>();
        List<String> rawLeaves = MerkleUtil.parseLeaves(data.getLeavesJson());
        for (int i = 0; i < rawLeaves.size(); i++) {
            ProofLeaf leaf = new ProofLeaf();
            leaf.setSeq(i + 1);
            leaf.setLeaf(rawLeaves.get(i));
            leaves.add(leaf);
        }
        proof.setLeaves(leaves);

        if (data.getTimeline() != null) {
            List<ProofEvent> events = new ArrayList<>();
            for (EventData e : data.getTimeline()) {
                ProofEvent event = new ProofEvent();
                event.setSeq(e.getSeq());
                event.setEventType(e.getEventType());
                event.setPayload(e);
                events.add(event);
            }
            proof.setEvents(events);
        }

        PublicChainStatus anchor = data.getAnchors() != null && !data.getAnchors().isEmpty()
                ? data.getAnchors().get(0)
                : null;
        if (anchor != null) {
            Anchor entity = new Anchor();
            entity.setChainId(anchor.getChainId());
            entity.setContractAddress(anchor.getContractAddress());
            entity.setTxHash(anchor.getTxHash());
            entity.setBlockNumber(anchor.getBlockNumber());
            entity.setStatus(anchor.getStatus());
            entity.setAnchoredAt(anchor.getAnchoredAt());
            entity.setExplorerUrl(anchor.getExplorerUrl());
            proof.setAnchor(entity);
        }
        return proof;
    }
}
