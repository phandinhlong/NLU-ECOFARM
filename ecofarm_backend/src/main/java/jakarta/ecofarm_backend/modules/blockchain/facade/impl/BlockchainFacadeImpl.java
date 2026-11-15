package jakarta.ecofarm_backend.modules.blockchain.facade.impl;

import jakarta.ecofarm_backend.common.dto.request.RecallRequest;
import jakarta.ecofarm_backend.common.dto.response.AnchorStatus;
import jakarta.ecofarm_backend.common.dto.response.PrivateChainStatus;
import jakarta.ecofarm_backend.common.dto.response.PublicChainStatus;
import jakarta.ecofarm_backend.common.dto.response.ProofBundle;
import jakarta.ecofarm_backend.common.dto.response.ReconciliationItem;
import jakarta.ecofarm_backend.config.BlockchainConfig;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.MerkleSnapshot;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Recall;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.AnchorRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.BatchRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.MerkleSnapshotRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.OutboxEventRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.RecallRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.UserRepository;
import jakarta.ecofarm_backend.modules.blockchain.client.PolygonClient;
import jakarta.ecofarm_backend.modules.blockchain.service.BlockchainAnchoringService;
import jakarta.ecofarm_backend.modules.blockchain.facade.BlockchainFacade;
import jakarta.ecofarm_backend.modules.blockchain.service.ReconciliationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class BlockchainFacadeImpl implements BlockchainFacade {

    @Inject
    private AnchorRepository anchorRepository;

    @Inject
    private OutboxEventRepository outboxEventRepository;

    @Inject
    private MerkleSnapshotRepository snapshotRepository;

    @Inject
    private ReconciliationService reconciliationService;

    @Inject
    private BlockchainConfig config;

    @Inject
    private RecallRepository recallRepository;

    @Inject
    private BatchRepository batchRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private PolygonClient polygonClient;

    @Inject
    private BlockchainAnchoringService anchoringService;

    @Override
    public List<Anchor> getAllAnchors() {
        return anchorRepository.findAll();
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
        Map<String, Object> resp = new java.util.LinkedHashMap<>();
        resp.put("page", page);
        resp.put("size", size);
        resp.put("totalElements", total);
        resp.put("totalPages", size > 0 ? (int) Math.ceil((double) total / size) : 0);
        resp.put("checkedAt", System.currentTimeMillis());
        resp.put("content", items.subList(from, to));
        return resp;
    }

    @Override
    public AnchorStatus getAnchorStatus(Long batchId) {
        AnchorStatus status = new AnchorStatus();
        status.setBatchId(batchId);

        MerkleSnapshot snapshot = snapshotRepository.findByBatchId(batchId);
        if (snapshot == null) {
            status.setBatchStatus("DRAFT");
            status.setPrivateChain(new PrivateChainStatus(false, null));
            PublicChainStatus publicChain = new PublicChainStatus();
            publicChain.setRequiredConfirmations(config.getRequiredConfirmations());
            publicChain.setStatus("PENDING");
            status.setPublicChain(publicChain);
            status.setAttempts(0);
            return status;
        }

        status.setSnapshot(new AnchorStatus.SnapshotInfo(
                snapshot.getId(), snapshot.getMerkleRoot(), snapshot.getLeafCount()));

        List<Anchor> anchors = anchorRepository.findBySnapshotId(snapshot.getId());
        if (anchors.isEmpty()) {
            status.setBatchStatus("ANCHORING");
            status.setPrivateChain(new PrivateChainStatus(false, null));
            PublicChainStatus publicChain = new PublicChainStatus();
            publicChain.setRequiredConfirmations(config.getRequiredConfirmations());
            publicChain.setStatus("PENDING");
            status.setPublicChain(publicChain);
            status.setAttempts(0);
            return status;
        }

        Anchor anchor = anchors.get(anchors.size() - 1);
        String statusStr = anchor.getStatus() != null ? anchor.getStatus() : "CONFIRMED";
        // Map anchor status onto the public BatchStatus enum of the spec
        switch (statusStr) {
            case "PENDING":
            case "SUBMITTED":
                status.setBatchStatus("ANCHORING");
                break;
            case "FAILED":
                status.setBatchStatus("ANCHOR_FAILED");
                break;
            default:
                status.setBatchStatus("ACTIVE");
                break;
        }

        status.setPrivateChain(new PrivateChainStatus("CONFIRMED".equals(statusStr), anchor.getTxHash()));

        PublicChainStatus publicChain = new PublicChainStatus();
        publicChain.setId(anchor.getId());
        publicChain.setSnapshotId(anchor.getSnapshot() != null ? anchor.getSnapshot().getId() : null);
        publicChain.setChainId(anchor.getChainId());
        publicChain.setContractAddress(anchor.getContractAddress());
        publicChain.setTxHash(anchor.getTxHash());
        publicChain.setBlockNumber(anchor.getBlockNumber());
        publicChain.setConfirmations(anchor.getConfirmations());
        publicChain.setStatus(statusStr);
        publicChain.setExplorerUrl(anchor.getExplorerUrl());
        publicChain.setAnchoredAt(anchor.getAnchoredAt());
        publicChain.setRequiredConfirmations(config.getRequiredConfirmations());
        status.setPublicChain(publicChain);

        status.setAttempts(anchors.size());
        status.setLastError(null);
        status.setNextAttemptAt(null);

        return status;
    }

    @Override
    public Anchor retryAnchor(Long anchorId) {
        Anchor anchor = anchorRepository.findById(anchorId);
        if (anchor == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if (!"FAILED".equals(anchor.getStatus())) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
        anchor.setStatus("PENDING");
        anchor.setAnchoredAt(System.currentTimeMillis());
        anchorRepository.persist(anchor);
        return anchor;
    }

    @Override
    public Recall recallBatch(Long batchId, RecallRequest req) {
        Recall recall = Recall.builder()
                .batch(batchRepository.findById(batchId))
                .reason(req.getReason())
                .createdByUser(userRepository.findById(1L).orElse(null))
                .build();
        recallRepository.persist(recall);
        return recall;
    }

    @Override
    public ProofBundle getProofBundle(Long batchId) {
        MerkleSnapshot snapshot = snapshotRepository.findByBatchId(batchId);
        ProofBundle bundle = new ProofBundle();
        bundle.setBatchCode("BATCH-" + batchId);
        bundle.setBatchIdHash("0x" + Long.toHexString(batchId));
        bundle.setHashAlgorithm("keccak256");
        bundle.setCanonicalization("RFC8785");
        bundle.setSalt("0xfaceb00c");

        if (snapshot != null) {
            bundle.setMerkleRoot(snapshot.getMerkleRoot());
        } else {
            bundle.setMerkleRoot("0xdeadbeefcafe1234");
        }

        bundle.setLeaves(new ArrayList<>());
        bundle.setEvents(new ArrayList<>());
        if (snapshot != null) {
            List<Anchor> anchors = anchorRepository.findBySnapshotId(snapshot.getId());
            if (!anchors.isEmpty()) {
                bundle.setAnchor(anchors.get(anchors.size() - 1));
            }
        }
        bundle.setGeneratedAt(System.currentTimeMillis());
        return bundle;
    }

    @Override
    public OutboxEvent createTestOutbox(String data) {
        OutboxEvent event = OutboxEvent.builder()
                .batchIdHash("0x" + Long.toHexString(System.currentTimeMillis()))
                .seq(1)
                .eventData(data)
                .status(OutboxEvent.OutboxStatus.PENDING)
                .attempts(0)
                .build();
        outboxEventRepository.persist(event);
        return event;
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
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("triggered", true);
        result.put("elapsedMs", elapsed);
        result.put("message", "processOutbox() executed manually");
        return result;
    }
}
