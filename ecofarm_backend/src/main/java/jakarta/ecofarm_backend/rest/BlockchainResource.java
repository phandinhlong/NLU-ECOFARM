package jakarta.ecofarm_backend.rest;

import jakarta.ecofarm_backend.persistence.entities.OutboxEvent;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import jakarta.ecofarm_backend.persistence.entities.Anchor;
import jakarta.ecofarm_backend.persistence.entities.MerkleSnapshot;
import jakarta.ecofarm_backend.persistence.entities.Recall;
import jakarta.ecofarm_backend.persistence.repository.AnchorRepository;
import jakarta.ecofarm_backend.persistence.repository.AuditLogRepository;
import jakarta.ecofarm_backend.persistence.repository.OutboxEventRepository;
import jakarta.ecofarm_backend.persistence.repository.MerkleSnapshotRepository;
import jakarta.ecofarm_backend.persistence.repository.RecallRepository;
import jakarta.ecofarm_backend.service.ReconciliationService;
import jakarta.ecofarm_backend.dto.AnchorStatus;
import jakarta.ecofarm_backend.dto.PrivateChainStatus;
import jakarta.ecofarm_backend.dto.PublicChainStatus;
import jakarta.ecofarm_backend.dto.ReconciliationItem;
import jakarta.ecofarm_backend.dto.ProofBundle;
import jakarta.ecofarm_backend.dto.RecallRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("/api/v1")
@Stateless
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
    private RecallRepository recallRepository;

    @GET
    @Path("/admin/anchors")
    public List<Anchor> getAllAnchors() {
        System.out.println(anchorRepository.findById(1L).getStatus());
        return anchorRepository.findAll();
    }
    @GET
    @Path("/admin/anchors/status")
    public String getStatusAnchors() {
        return anchorRepository.findById(2L).getStatus();
    }

    @GET
    @Path("/admin/reconciliation")
    public List<ReconciliationItem> getReconciliation(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("onlyMismatch") boolean onlyMismatch
    ) {
        List<ReconciliationItem> items = reconciliationService.reconcileAll();
        if (onlyMismatch) {
            items = new ArrayList<>();
        }
        return items;
    }

    @GET
    @Path("/batches/{batchId}/anchor-status")
    public AnchorStatus getAnchorStatus(@PathParam("batchId") Long batchId) {
        AnchorStatus status = new AnchorStatus();
        status.setBatchId(batchId);

        MerkleSnapshot snapshot = snapshotRepository.findByBatchId(batchId);
        if (snapshot == null) {
            status.setBatchStatus("NOT_FOUND");
            PrivateChainStatus privateChain = new PrivateChainStatus(true, null);
            PublicChainStatus publicChain = new PublicChainStatus();
            publicChain.setStatus("NOT_AVAILABLE");
            status.setPrivateChain(privateChain);
            status.setPublicChain(publicChain);
            status.setAttempts(0);
            return status;
        }

        List<Anchor> anchors = anchorRepository.findBySnapshotId(snapshot.getId());
        if (anchors.isEmpty()) {
            status.setBatchStatus("ANCHORING");
            PrivateChainStatus privateChain = new PrivateChainStatus(true, null);
            PublicChainStatus publicChain = new PublicChainStatus();
            publicChain.setStatus("PENDING");
            status.setPrivateChain(privateChain);
            status.setPublicChain(publicChain);
            status.setAttempts(0);
            return status;
        }

        Anchor anchor = anchors.get(anchors.size() - 1);
        String statusStr = anchor.getStatus() != null ? anchor.getStatus() : "CONFIRMED";
        status.setBatchStatus(statusStr);

        PrivateChainStatus privateChain = new PrivateChainStatus(true, anchor.getTxHash());
        status.setPrivateChain(privateChain);

        PublicChainStatus publicChain = new PublicChainStatus();
        publicChain.setTxHash(anchor.getTxHash());
        publicChain.setBlockNumber(anchor.getBlockNumber());
        publicChain.setConfirmations(anchor.getConfirmations());
        publicChain.setStatus(statusStr);
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
        anchor.setStatus("ANCHORING");
        anchor.setAnchoredAt(System.currentTimeMillis() / 1000);
        anchorRepository.persist(anchor);
        System.out.println(anchor);
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
        bundle.setGeneratedAt(Instant.now());
        return bundle;
    }
    // dùng đế test publicTxHash gửi lên polygon
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

}
