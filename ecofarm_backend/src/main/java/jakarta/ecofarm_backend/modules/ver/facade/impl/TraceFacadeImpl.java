package jakarta.ecofarm_backend.modules.ver.facade.impl;

import jakarta.ecofarm_backend.common.dto.response.BatchInfo;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.BlockchainStatus;
import jakarta.ecofarm_backend.common.dto.response.EventData;
import jakarta.ecofarm_backend.common.dto.response.GreenhouseInfo;
import jakarta.ecofarm_backend.common.dto.response.ProofBundle;
import jakarta.ecofarm_backend.common.dto.response.ProofBundleResponse;
import jakarta.ecofarm_backend.common.dto.response.ProofEvent;
import jakarta.ecofarm_backend.common.dto.response.ProofLeaf;
import jakarta.ecofarm_backend.common.dto.response.PublicChainStatus;
import jakarta.ecofarm_backend.common.dto.response.PublicTrace;
import jakarta.ecofarm_backend.common.dto.response.VarietyInfo;
import jakarta.ecofarm_backend.common.dto.response.VerifyResult;
import jakarta.ecofarm_backend.common.enums.AnchorStatus;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.enums.VerificationStatus;
import jakarta.ecofarm_backend.common.exception.EntityNotFoundException;
import jakarta.ecofarm_backend.config.BlockchainConfig;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.VerTraceRepository;
import jakarta.ecofarm_backend.modules.blockchain.client.BesuClient;
import jakarta.ecofarm_backend.modules.blockchain.client.BlockchainClient;
import jakarta.ecofarm_backend.modules.blockchain.client.PolygonClient;
import jakarta.ecofarm_backend.modules.packaging.service.MerkleTreeService;
import jakarta.ecofarm_backend.modules.ver.cache.AnchorRootCache;
import jakarta.ecofarm_backend.modules.ver.facade.TraceFacade;
import jakarta.ecofarm_backend.modules.ver.repository.TraceData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class TraceFacadeImpl implements TraceFacade {

    private static final Logger log = LogManager.getLogger(TraceFacadeImpl.class);

    private static final String RESULT_MATCH = "MATCH";
    private static final String RESULT_MISMATCH = "MISMATCH";
    private static final String RESULT_UNAVAILABLE = "UNAVAILABLE";

    private static final String GET_MERKLE_ROOT = "getMerkleRoot";

    @Inject
    private VerTraceRepository traceRepository;

    @Inject
    private PolygonClient polygonClient;

    @Inject
    private BesuClient besuClient;

    @Inject
    private BlockchainConfig blockchainConfig;

    @Inject
    private MerkleTreeService merkleTreeService;

    @Inject
    private AnchorRootCache anchorRootCache;

    @Override
    public BaseResponse<PublicTrace> getTrace(String code) {
        log.info("getTrace code={}", code);
        return BaseResponse.build(mapToResponse(requireTrace(code)));
    }

    @Override
    public BaseResponse<VerifyResult> verifyTrace(String code) {
        return BaseResponse.build(doVerify(code, requireTrace(code)));
    }

    @Override
    public BaseResponse<ProofBundleResponse> getProofBundle(String code) {
        TraceData data = requireTrace(code);
        return BaseResponse.build(ProofBundleResponse.builder()
                .code(data.getCode())
                .trace(mapToResponse(data))
                .proof(mapProof(data))
                .verification(doVerify(code, data))
                .build());
    }

    private TraceData requireTrace(String code) {
        TraceData data = traceRepository.load(code);
        if (data == null) {
            throw new EntityNotFoundException(ErrorCode.QR_NOT_FOUND);
        }
        return data;
    }

    // Cùng quy ước batchKey -> batchIdHash với module packaging để contract đọc đúng root
    private String batchKey(TraceData data) {
        return data.getBatchCode() != null ? data.getBatchCode() : String.valueOf(data.getBatchId());
    }

    private BlockchainClient selectClient(Integer chainId) {
        if (chainId == null) {
            return null;
        }
        if (chainId.equals(blockchainConfig.getPublicChainId())) {
            return polygonClient;
        }
        if (isPrivateChain(chainId)) {
            return besuClient;
        }
        return null;
    }

    private boolean isPrivateChain(Integer chainId) {
        try {
            return blockchainConfig.getPrivateNetworkId() != null
                    && Long.parseLong(blockchainConfig.getPrivateNetworkId()) == chainId.longValue();
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isPublicChain(Integer chainId) {
        return chainId != null && chainId.equals(blockchainConfig.getPublicChainId());
    }

    private String chainName(Integer chainId) {
        if (isPublicChain(chainId)) {
            return "Polygon Amoy";
        }
        if (isPrivateChain(chainId)) {
            return "Besu Private";
        }
        return chainId != null ? "Chain " + chainId : null;
    }

    // contract BlockchainAnchoring: getMerkleRoot(bytes32 batchId) -> bytes32
    private String buildGetRootCalldata(TraceData data) {
        String batchIdHash = Hash.sha3String(batchKey(data));
        Function function = new Function(GET_MERKLE_ROOT,
                Collections.singletonList(new Bytes32(Numeric.hexStringToByteArray(batchIdHash))),
                Collections.emptyList());
        return FunctionEncoder.encode(function);
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
        if (storedRoot == null || data.getEvents() == null || data.getEvents().isEmpty()) {
            return builder.result(RESULT_UNAVAILABLE)
                    .message("Chưa có snapshot Merkle cho mã này")
                    .build();
        }

        String computedRoot;
        try {
            computedRoot = merkleTreeService.buildTree(data.getSaltEnc(), data.getEvents()).merkleRoot();
        } catch (IllegalArgumentException e) {
            return builder.result(RESULT_UNAVAILABLE)
                    .message("Snapshot không có sự kiện để dựng lại Merkle root")
                    .build();
        }
        builder.computedRoot(computedRoot);

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
        BlockchainClient client = selectClient(chainId);
        if (client == null) {
            return builder.result(RESULT_UNAVAILABLE)
                    .message("Chain " + chainId + " chưa được hỗ trợ xác thực (Polygon Amoy và private chain Besu)")
                    .build();
        }

        // Cache root tren chain de RPC sap khong chan luong xac thuc
        String cacheKey = chainId + ":" + anchor.getContractAddress();
        String onChainRoot = anchorRootCache.get(cacheKey);
        if (onChainRoot == null) {
            try {
                onChainRoot = client.callContract(anchor.getContractAddress(), buildGetRootCalldata(data));
            } catch (Exception e) {
                log.warn("Không đọc được Merkle root trên chain {}: {}", chainId, e.getMessage());
                onChainRoot = null;
            }
            if (onChainRoot != null) {
                anchorRootCache.put(cacheKey, onChainRoot);
            }
        }
        builder.onChainRoot(onChainRoot);
        if (onChainRoot == null) {
            return builder.result(RESULT_UNAVAILABLE)
                    .message("Không đọc được root trên " + chainName(chainId) + ", vui lòng thử lại sau")
                    .build();
        }

        if (onChainRoot.equalsIgnoreCase(computedRoot)) {
            return builder.result(RESULT_MATCH)
                    .message("Dữ liệu khớp với root trên " + chainName(chainId) + " — xác thực thành công")
                    .build();
        }
        return builder.result(RESULT_MISMATCH)
                .message("Root trên chain khác root tính lại — dữ liệu đã bị chỉnh sửa sau khi neo")
                .build();
    }

    private PublicTrace mapToResponse(TraceData data) {
        PublicTrace.PublicTraceBuilder trace = PublicTrace.builder().code(data.getCode());

        if (data.getBatchCode() != null) {
            trace.batch(BatchInfo.builder()
                    .batchCode(data.getBatchCode())
                    .status(data.getBatchStatus())
                    .varietyName(data.getVarietyName())
                    .greenhouseCode(data.getGreenhouseCode())
                    .greenhouseName(data.getGreenhouseName())
                    .sowDate(data.getSowDate())
                    .harvestDate(data.getHarvestDate())
                    .activatedAt(data.getActivatedAt())
                    .active(data.getBatchActive())
                    .recalled(data.getRecalled())
                    .build());
        }

        if (data.getVarietyCode() != null) {
            trace.variety(VarietyInfo.builder()
                    .code(data.getVarietyCode())
                    .name(data.getVarietyName())
                    .build());
        }
        if (data.getGreenhouseCode() != null) {
            trace.greenhouse(GreenhouseInfo.builder()
                    .code(data.getGreenhouseCode())
                    .name(data.getGreenhouseName())
                    .build());
        }

        trace.certificates(data.getCertificates());
        trace.metrics(data.getMetrics());
        trace.timeline(data.getTimeline());
        if (data.getMerkleRoot() != null) {
            trace.blockchain(mapBlockchain(data));
        }
        if (data.getRecalls() != null && !data.getRecalls().isEmpty()) {
            trace.recall(data.getRecalls().get(0));
        }
        return trace.build();
    }

    private BlockchainStatus mapBlockchain(TraceData data) {
        PublicChainStatus anchor = data.getAnchors() != null && !data.getAnchors().isEmpty()
                ? data.getAnchors().get(0)
                : null;

        String chainName = null;
        String txHash = null;
        Long anchoredAt = null;
        String explorerUrl = null;

        if (anchor != null) {
            chainName = chainName(anchor.getChainId());
            txHash = anchor.getTxHash();
            anchoredAt = anchor.getAnchoredAt();
            explorerUrl = anchor.getExplorerUrl();
            if (explorerUrl == null && anchor.getTxHash() != null && isPublicChain(anchor.getChainId())) {
                explorerUrl = "https://amoy.polygonscan.com/tx/" + anchor.getTxHash();
            }
        }

        VerificationStatus status = VerificationStatus.fromAnchorStatus(anchorStatusOf(anchor));

        return BlockchainStatus.builder()
                .anchored(status == VerificationStatus.VERIFIED)
                .chainName(chainName)
                .merkleRoot(data.getMerkleRoot())
                .txHash(txHash)
                .anchoredAt(anchoredAt)
                .explorerUrl(explorerUrl)
                .status(status.name())
                .leafCount(data.getLeafCount())
                .chainId(anchor != null ? anchor.getChainId() : null)
                .contractAddress(anchor != null ? anchor.getContractAddress() : null)
                .blockNumber(anchor != null ? anchor.getBlockNumber() : null)
                .build();
    }

    private static AnchorStatus anchorStatusOf(PublicChainStatus anchor) {
        if (anchor == null || anchor.getStatus() == null) {
            return null;
        }
        try {
            return AnchorStatus.valueOf(anchor.getStatus());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private ProofBundle mapProof(TraceData data) {
        List<String> leafHashes = buildLeafHashes(data);
        List<CultivationEvent> events = data.getEvents() != null ? data.getEvents() : List.of();
        List<ProofLeaf> leaves = new ArrayList<>();
        for (int i = 0; i < leafHashes.size(); i++) {
            ProofLeaf leaf = new ProofLeaf();
            leaf.setSeq(i + 1);
            leaf.setEventHash(i < events.size() ? events.get(i).getEventHash() : null);
            leaf.setLeaf(leafHashes.get(i));
            leaves.add(leaf);
        }

        List<ProofEvent> proofEvents = new ArrayList<>();
        if (data.getTimeline() != null) {
            for (EventData e : data.getTimeline()) {
                ProofEvent event = new ProofEvent();
                event.setSeq(e.getSeq());
                event.setEventType(e.getEventType());
                event.setPayload(e);
                proofEvents.add(event);
            }
        }

        PublicChainStatus anchor = data.getAnchors() != null && !data.getAnchors().isEmpty()
                ? data.getAnchors().get(0)
                : null;
        Anchor anchorEntity = null;
        if (anchor != null) {
            anchorEntity = new Anchor();
            anchorEntity.setChainId(anchor.getChainId());
            anchorEntity.setContractAddress(anchor.getContractAddress());
            anchorEntity.setTxHash(anchor.getTxHash());
            anchorEntity.setBlockNumber(anchor.getBlockNumber());
            anchorEntity.setStatus(anchor.getStatus());
            anchorEntity.setAnchoredAt(anchor.getAnchoredAt());
            anchorEntity.setExplorerUrl(anchor.getExplorerUrl());
        }

        return ProofBundle.builder()
                .batchCode(data.getBatchCode())
                .batchIdHash(Hash.sha3String(batchKey(data)))
                .hashAlgorithm("keccak256")
                .canonicalization("RFC8785")
                .salt(data.getSaltEnc() != null ? String.valueOf(data.getSaltEnc()) : null)
                .merkleRoot(data.getMerkleRoot())
                .generatedAt(System.currentTimeMillis())
                .leaves(leaves)
                .events(proofEvents)
                .anchor(anchorEntity)
                .build();
    }

    private List<String> buildLeafHashes(TraceData data) {
        if (data.getEvents() == null || data.getEvents().isEmpty()) {
            return List.of();
        }
        try {
            return merkleTreeService.buildTree(data.getSaltEnc(), data.getEvents()).leafHashes();
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }
}
