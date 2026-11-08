package jakarta.ecofarm_backend.rest;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ecofarm_backend.persistence.entities.Anchor;
import jakarta.ecofarm_backend.persistence.entities.OutboxEvent;
import jakarta.ecofarm_backend.persistence.entities.MerkleSnapshot;
import jakarta.ecofarm_backend.persistence.repository.AnchorRepository;
import jakarta.ecofarm_backend.persistence.repository.AuditLogRepository;
import jakarta.ecofarm_backend.persistence.repository.OutboxEventRepository;
import jakarta.ecofarm_backend.persistence.repository.MerkleSnapshotRepository;
import jakarta.ecofarm_backend.service.ReconciliationService;
import jakarta.ecofarm_backend.dto.AnchorStatus;
import jakarta.ecofarm_backend.dto.PrivateChainStatus;
import jakarta.ecofarm_backend.dto.PublicChainStatus;
import jakarta.ecofarm_backend.dto.ReconciliationItem;

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

    @GET
    @Path("/admin/reconciliation")
    public List<ReconciliationItem> getReconciliation(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("onlyMismatch") @DefaultValue("false") boolean onlyMismatch
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
        status.setBatchStatus("ANCHORING");

        PrivateChainStatus privateChain = new PrivateChainStatus(true, "0xPRIVATE_TX");
        status.setPrivateChain(privateChain);

        PublicChainStatus publicChain = new PublicChainStatus();
        publicChain.setTxHash("0xPUBLIC_TX");
        publicChain.setBlockNumber(12345678L);
        publicChain.setConfirmations(15);
        publicChain.setStatus("CONFIRMED");
        status.setPublicChain(publicChain);

        status.setAttempts(2);
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
        anchor.setAnchoredAt(Instant.now());
        anchorRepository.persist(anchor);
        return anchor;
    }

}
