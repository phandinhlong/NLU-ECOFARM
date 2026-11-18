package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.common.dto.request.RecallRequest;
import jakarta.ecofarm_backend.common.dto.response.PrivateChainStatus;
import jakarta.ecofarm_backend.common.dto.response.ProofBundle;
import jakarta.ecofarm_backend.common.dto.response.PublicChainStatus;
import jakarta.ecofarm_backend.common.dto.response.ReconciliationItem;
import jakarta.ecofarm_backend.common.enums.AnchorEvent;
import jakarta.ecofarm_backend.common.enums.AnchorStatus;
import jakarta.ecofarm_backend.common.enums.BatchStatus;
import jakarta.ecofarm_backend.config.BlockchainConfig;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.MerkleSnapshot;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Recall;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.service.AnchorService;
import jakarta.ecofarm_backend.infrastructure.service.BatchService;
import jakarta.ecofarm_backend.infrastructure.service.BlockchainAnchoringService;
import jakarta.ecofarm_backend.infrastructure.service.BlockchainService;
import jakarta.ecofarm_backend.infrastructure.service.MerkleSnapshotService;
import jakarta.ecofarm_backend.infrastructure.service.OutboxEventService;
import jakarta.ecofarm_backend.infrastructure.service.RecallService;
import jakarta.ecofarm_backend.infrastructure.service.ReconciliationService;
import jakarta.ecofarm_backend.modules.blockchain.client.PolygonClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class BlockchainServiceImpl implements BlockchainService {

    @Inject
    private AnchorService anchorService;

    @Inject
    private OutboxEventService outboxEventService;

    @Inject
    private MerkleSnapshotService snapshotService;

    @Inject
    private BatchService batchService;

    @Inject
    private RecallService recallService;

    @Inject
    private ReconciliationService reconciliationService;

    @Inject
    private BlockchainAnchoringService anchoringService;

    @Inject
    private PolygonClient polygonClient;

    @Inject
    private BlockchainConfig config;

    @Override
    public List<Anchor> getAllAnchors() {
        return anchorService.findAll();
    }

    @Override
    public Map<String, Object> getReconciliation(int page, int size, boolean onlyMismatch) {
        List<ReconciliationItem> items = reconciliationService.reconcileAll();
        if (onlyMismatch) {
            List<ReconciliationItem> mismatch = new ArrayList<>();
            for (ReconciliationItem item : items) {
                if (!"OK".equals(item.getResult())) {
                    mismatch.add(item);
                }
            }
            items = mismatch;
        }
        int total = items.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("page", page);
        resp.put("size", size);
        resp.put("totalElements", total);
        resp.put("totalPages", size > 0 ? (int) Math.ceil((double) total / size) : 0);
        resp.put("checkedAt", System.currentTimeMillis());
        resp.put("content", items.subList(from, to));
        return resp;
    }

    @Override
    public jakarta.ecofarm_backend.common.dto.response.AnchorStatus getAnchorStatus(Long batchId) {
        MerkleSnapshot snapshot = snapshotService.findByBatchId(batchId);
        if (snapshot == null) {
            return jakarta.ecofarm_backend.common.dto.response.AnchorStatus.builder()
                    .batchId(batchId)
                    .batchStatus(BatchStatus.DRAFT.name())
                    .privateChain(PrivateChainStatus.builder().finalized(false).txHash(null).build())
                    .publicChain(PublicChainStatus.builder()
                            .requiredConfirmations(config.getRequiredConfirmations())
                            .status(AnchorStatus.PENDING.name())
                            .build())
                    .attempts(0)
                    .build();
        }

        jakarta.ecofarm_backend.common.dto.response.AnchorStatus.SnapshotInfo snapshotInfo = jakarta.ecofarm_backend.common.dto.response.AnchorStatus.SnapshotInfo.builder()
                .id(snapshot.getId())
                .merkleRoot(snapshot.getMerkleRoot())
                .leafCount(snapshot.getLeafCount())
                .build();

        List<Anchor> anchors = anchorService.findBySnapshotId(snapshot.getId());
        if (anchors.isEmpty()) {
            return jakarta.ecofarm_backend.common.dto.response.AnchorStatus.builder()
                    .batchId(batchId)
                    .snapshot(snapshotInfo)
                    .batchStatus(BatchStatus.ANCHORING.name())
                    .privateChain(PrivateChainStatus.builder().finalized(false).txHash(null).build())
                    .publicChain(PublicChainStatus.builder()
                            .requiredConfirmations(config.getRequiredConfirmations())
                            .status(AnchorStatus.PENDING.name())
                            .build())
                    .attempts(0)
                    .build();
        }

        Anchor anchor = anchors.get(anchors.size() - 1);
        AnchorStatus anchorStatus = AnchorStatus.fromString(anchor.getStatus());
        BatchStatus batchStatus = anchorStatus.toBatchStatus();

        PrivateChainStatus privateChain = PrivateChainStatus.builder()
                .finalized(AnchorStatus.CONFIRMED == anchorStatus)
                .txHash(anchor.getTxHash())
                .build();

        PublicChainStatus publicChain = PublicChainStatus.builder()
                .id(anchor.getId())
                .snapshotId(anchor.getSnapshot() != null ? anchor.getSnapshot().getId() : null)
                .chainId(anchor.getChainId())
                .contractAddress(anchor.getContractAddress())
                .txHash(anchor.getTxHash())
                .blockNumber(anchor.getBlockNumber())
                .confirmations(anchor.getConfirmations())
                .status(anchorStatus.name())
                .explorerUrl(anchor.getExplorerUrl())
                .anchoredAt(anchor.getAnchoredAt())
                .requiredConfirmations(config.getRequiredConfirmations())
                .build();

        return jakarta.ecofarm_backend.common.dto.response.AnchorStatus.builder()
                .batchId(batchId)
                .batchStatus(batchStatus.name())
                .snapshot(snapshotInfo)
                .privateChain(privateChain)
                .publicChain(publicChain)
                .attempts(anchors.size())
                .lastError(null)
                .nextAttemptAt(null)
                .build();
    }

    @Override
    public Anchor retryAnchor(Long anchorId) {
        Anchor anchor = anchorService.findById(anchorId);
        if (anchor == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        AnchorStatus current = AnchorStatus.fromString(anchor.getStatus());
        if (!current.canFire(AnchorEvent.RETRY)) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
        anchor.setStatus(current.fire(AnchorEvent.RETRY).name());
        anchor.setAnchoredAt(System.currentTimeMillis());
        anchorService.persist(anchor);
        return anchor;
    }

    @Override
    public Recall recallBatch(Long batchId, RecallRequest req, User currentUser) {
        Recall recall = Recall.builder()
                .batch(batchService.findById(batchId))
                .reason(req.getReason())
                .createdByUser(currentUser)
                .build();
        recallService.persist(recall);
        return recall;
    }

    @Override
    public ProofBundle getProofBundle(Long batchId) {
        MerkleSnapshot snapshot = snapshotService.findByBatchId(batchId);
        Batch batch = batchService.findById(batchId);
        String batchCode = batch != null && batch.getBatchCode() != null ? batch.getBatchCode() : "BATCH-" + batchId;
        String merkleRoot = snapshot != null ? snapshot.getMerkleRoot() : "0xdeadbeefcafe1234";

        Anchor lastAnchor = null;
        if (snapshot != null) {
            List<Anchor> anchors = anchorService.findBySnapshotId(snapshot.getId());
            if (!anchors.isEmpty()) {
                lastAnchor = anchors.get(anchors.size() - 1);
            }
        }

        return ProofBundle.builder()
                .batchCode(batchCode)
                .batchIdHash("0x" + Long.toHexString(batchId))
                .hashAlgorithm("keccak256")
                .canonicalization("RFC8785")
                .salt("0xfaceb00c")
                .merkleRoot(merkleRoot)
                .leaves(new ArrayList<>())
                .events(new ArrayList<>())
                .anchor(lastAnchor)
                .generatedAt(System.currentTimeMillis())
                .build();
    }

    @Override
    public OutboxEvent createTestOutbox(String data) {
        return outboxEventService.createTestOutbox(data);
    }

    @Override
    public Map<String, Object> getPolygonStatus() throws Exception {
        polygonClient.connect();
        boolean connected = polygonClient.isConnected();
        long currentBlock = polygonClient.getCurrentBlockNumber();
        return Map.of(
                "connected", connected,
                "chainId", 80002,
                "currentBlockNumber", currentBlock
        );
    }

    @Override
    public Map<String, Object> getPolygonConfirmations(String txHash) throws Exception {
        int confirmations = polygonClient.getConfirmations(txHash);
        return Map.of(
                "txHash", txHash,
                "confirmations", confirmations,
                "explorerUrl", "https://amoy.polygonscan.com/tx/" + txHash
        );
    }

    @Override
    public Map<String, Object> getTransactionDetail(String txHash) throws Exception {
        return polygonClient.getTransactionInfo(txHash);
    }

    @Override
    public Map<String, Object> triggerProcessOutbox() {
        long before = System.currentTimeMillis();
        anchoringService.processOutbox();
        long elapsed = System.currentTimeMillis() - before;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("triggered", true);
        result.put("elapsedMs", elapsed);
        result.put("message", "processOutbox() executed manually");
        return result;
    }
}
