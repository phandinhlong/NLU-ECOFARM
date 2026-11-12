package jakarta.ecofarm_backend.modules.blockchain.rest;


import jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent;
import jakarta.ecofarm_backend.modules.blockchain.PolygonClient;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.MerkleSnapshot;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Recall;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.AnchorRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.AuditLogRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.BatchRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.OutboxEventRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.MerkleSnapshotRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.RecallRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.TraceLookupRepository;
import jakarta.ecofarm_backend.modules.blockchain.service.ReconciliationService;
import jakarta.ecofarm_backend.config.BlockchainConfig;
import jakarta.ecofarm_backend.common.dto.AnchorStatus;
import jakarta.ecofarm_backend.common.dto.PrivateChainStatus;
import jakarta.ecofarm_backend.common.dto.PublicChainStatus;
import jakarta.ecofarm_backend.common.dto.ReconciliationItem;
import jakarta.ecofarm_backend.common.dto.ProofBundle;
import jakarta.ecofarm_backend.common.dto.ProofEvent;
import jakarta.ecofarm_backend.common.dto.ProofLeaf;
import jakarta.ecofarm_backend.common.dto.RecallRequest;
import jakarta.ecofarm_backend.common.util.MerkleUtil;


import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("/v1/block-chain")
@Stateless
@RequireRoles(value = {"ADMIN", "QUALITY_CONTROL", "FARMER"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BlockchainResource {

    @Inject
    private AnchorRepository anchorRepository;

    @Inject
    private OutboxEventRepository outboxEventRepository;

    @Inject
    private MerkleSnapshotRepository snapshotRepository;

    @Inject
    private AuditLogRepository auditLogRepository;

    @Inject
    private ReconciliationService reconciliationService;

    @Inject
    private BlockchainConfig config;

    @Inject
    private RecallRepository recallRepository;

    @Inject
    private PolygonClient polygonClient;

    @Inject
    private BatchRepository batchRepository;

    @Inject
    private TraceLookupRepository traceLookupRepository;

//    @GET
//    @Path("/admin/anchors")
//    public List<Anchor> getAllAnchors() {
//        System.out.println(anchorRepository.findById(1L).getStatus());
//        return anchorRepository.findAll();
//    }
    @GET
    @Path("/admin/anchors")
    public List<Anchor> getAllAnchors() {
        return anchorRepository.findAll();
    }

    @GET
    @Path("/admin/reconciliation")
    public Response getReconciliation(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("onlyMismatch") boolean onlyMismatch
    ) {
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
        return Response.ok(resp).build();
    }


    @GET
    @Path("/batches/{batchId}/anchor-status")
    public AnchorStatus getAnchorStatus(@PathParam("batchId") Long batchId) {
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
        publicChain.setSnapshotId(anchor.getSnapshotId());
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

    @POST
    @Path("/admin/anchors/{anchorId}/retry")
    public Anchor retryAnchor(@PathParam("anchorId") Long anchorId) {
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

    @POST
    @Path("/batches/{batchId}/recall")
    public Recall recallBatch(@PathParam("batchId") Long batchId, RecallRequest req) {
        Recall recall = new Recall(batchId, req.getReason(), 1L);
        recallRepository.persist(recall);
        return recall;
    }

    @GET
    @Path("/batches/{batchId}/proof-bundle")
    public ProofBundle getProofBundle(@PathParam("batchId") Long batchId) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

        ProofBundle bundle = new ProofBundle();
        bundle.setBatchCode(batch.getBatchCode());
        bundle.setBatchIdHash(MerkleUtil.keccak256Hex(batch.getBatchCode().getBytes(StandardCharsets.UTF_8)));
        bundle.setHashAlgorithm("keccak256");
        bundle.setCanonicalization("UTF-8");
        bundle.setSalt(batch.getSaltEnc() == null ? null : String.format("0x%016x", batch.getSaltEnc()));
        bundle.setEvents(buildAnonymizedEvents(batchId));
        bundle.setSelfCheckScript(SELF_CHECK_SCRIPT);

        MerkleSnapshot snapshot = snapshotRepository.findByBatchId(batchId);
        if (snapshot == null) {
            bundle.setMerkleRoot(null);
            bundle.setLeaves(new ArrayList<>());
            bundle.setGeneratedAt(System.currentTimeMillis());
            return bundle;
        }

        bundle.setMerkleRoot(snapshot.getMerkleRoot());
        List<String> leafStrings = MerkleUtil.parseLeaves(snapshotRepository.findLeavesJson(snapshot.getId()));
        List<ProofLeaf> proofLeaves = new ArrayList<>();
        for (int i = 0; i < leafStrings.size(); i++) {
            ProofLeaf leaf = new ProofLeaf();
            leaf.setSeq(i + 1);
            leaf.setEventHash(leafStrings.get(i));
            leaf.setLeaf(leafStrings.get(i));
            leaf.setProof(MerkleUtil.buildProof(leafStrings, i));
            proofLeaves.add(leaf);
        }
        bundle.setLeaves(proofLeaves);

        Optional<Anchor> confirmed = anchorRepository.findLatestConfirmedBySnapshotId(snapshot.getId());
        if (confirmed.isPresent()) {
            bundle.setAnchor(confirmed.get());
        } else {
            List<Anchor> anchors = anchorRepository.findBySnapshotId(snapshot.getId());
            if (!anchors.isEmpty()) {
                bundle.setAnchor(anchors.get(anchors.size() - 1));
            }
        }
        bundle.setGeneratedAt(System.currentTimeMillis());
        return bundle;
    }

    // Khử thông tin cá nhân: chỉ giữ seq, loại sự kiện, hash và thời điểm ghi nhận
    private List<ProofEvent> buildAnonymizedEvents(Long batchId) {
        return traceLookupRepository.findCultivationEventsByBatchId(batchId).stream()
                .sorted(Comparator.comparing(CultivationEvent::getSeq,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(event -> {
                    ProofEvent proofEvent = new ProofEvent();
                    proofEvent.setSeq(event.getSeq());
                    proofEvent.setEventType(event.getEventType());
                    Map<String, Object> payload = new java.util.LinkedHashMap<>();
                    payload.put("eventHash", event.getEventHash());
                    payload.put("recordedAt", event.getRecordedAt());
                    proofEvent.setPayload(payload);
                    return proofEvent;
                })
                .toList();
    }

    private static final String SELF_CHECK_SCRIPT = """
            // EcoFarm - Script tu kiem tra goi bang chung (proof bundle)
            // Dung: node verify-proof.js <proof-bundle.json>
            // Yeu cau: npm install ethers
            const fs = require('fs');
            const { keccak256, toUtf8Bytes, hexlify, concat, getBytes } = require('ethers');

            function leafBytes(leaf) {
              if (typeof leaf === 'string' && leaf.startsWith('0x') && leaf.length % 2 === 0) {
                return getBytes(leaf);
              }
              return toUtf8Bytes(String(leaf));
            }

            function hashPair(leftHex, rightHex) {
              return keccak256(concat([getBytes(leftHex), getBytes(rightHex)]));
            }

            function computeRoot(leaves) {
              if (!leaves || leaves.length === 0) return null;
              let level = leaves.map(leafBytes).map(hexlify);
              while (level.length > 1) {
                const next = [];
                for (let i = 0; i < level.length; i += 2) {
                  const right = i + 1 < level.length ? level[i + 1] : level[i];
                  next.push(hashPair(level[i], right));
                }
                level = next;
              }
              return level[0];
            }

            function verifyProof(leaf, proof, root, index) {
              let hash = leafBytes(leaf);
              let idx = index;
              for (const siblingHex of proof) {
                const sibling = getBytes(siblingHex);
                hash = getBytes(keccak256(concat([
                  idx % 2 === 0 ? hash : sibling,
                  idx % 2 === 0 ? sibling : hash
                ])));
                idx = Math.floor(idx / 2);
              }
              return hexlify(hash).toLowerCase() === String(root).toLowerCase();
            }

            const bundle = JSON.parse(fs.readFileSync(process.argv[2], 'utf8'));
            if (!bundle.leaves || bundle.leaves.length === 0) {
              console.error('Proof bundle khong co leaves.');
              process.exit(2);
            }
            const root = computeRoot(bundle.leaves.map(l => l.leaf));
            console.log('Batch code     :', bundle.batchCode);
            console.log('Salt           :', bundle.salt);
            console.log('Computed root  :', root);
            console.log('Bundle root    :', bundle.merkleRoot);
            const rootMatch = root !== null && root.toLowerCase() === String(bundle.merkleRoot).toLowerCase();
            console.log('Root match     :', rootMatch);
            let allProofsOk = true;
            bundle.leaves.forEach((l, i) => {
              const ok = verifyProof(l.leaf, l.proof || [], bundle.merkleRoot, i);
              if (!ok) allProofsOk = false;
              console.log('Leaf #' + i + ' (' + l.eventHash + ') proof: ' + (ok ? 'OK' : 'FAILED'));
            });
            process.exit(rootMatch && allProofsOk ? 0 : 1);
            """;

    // API tạo sự kiện test gửi dữ liệu lên Blockchain (Besu & Polygon)
    @POST
    @Path("/admin/outbox/test")
    public OutboxEvent createTestOutbox(@QueryParam("data") @DefaultValue("EcoFarm Polygon Test") String data) {
        OutboxEvent event = new OutboxEvent(
                "0x" + Long.toHexString(System.currentTimeMillis()),
                1,
                data
        );
        outboxEventRepository.persist(event);
        return event;
    }
    // 1. Test kết nối & lấy số Block hiện tại trên mạng Polygon Amoy
    @GET
    @Path("/admin/polygon/status")
    public Map<String, Object> testPolygonStatus() throws Exception {
        polygonClient.connect();
        boolean connected = polygonClient.isConnected();
        long currentBlock = polygonClient.getCurrentBlockNumber();
        return Map.of(
                "connected", connected,
                "chainId", 80002,
                "currentBlockNumber", currentBlock
        );
    }
    // 2. Test kiểm tra số Confirmations của 1 txHash trên Polygon
    @GET
    @Path("/admin/polygon/confirmations/{txHash}")
    public Map<String, Object> testPolygonConfirmations(@PathParam("txHash") String txHash) throws Exception {
        int confirmations = polygonClient.getConfirmations(txHash);
        return Map.of(
                "txHash", txHash,
                "confirmations", confirmations,
                "explorerUrl", "https://amoy.polygonscan.com/tx/" + txHash
        );
    }
    @GET
    @Path("/admin/transactions/{txHash}")
    public Response getTransactionDetail(@PathParam("txHash") String txHash) {
        try {
            java.util.Map<String, Object> detail = polygonClient.getTransactionInfo(txHash);
            return Response.ok(detail).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(java.util.Map.of("error", e.getMessage()))
                    .build();
        }
    }

}
