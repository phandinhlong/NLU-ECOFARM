package jakarta.ecofarm_backend.modules.blockchain;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.request.RecallRequest;
import jakarta.ecofarm_backend.common.dto.response.AnchorStatus;
import jakarta.ecofarm_backend.common.dto.response.ProofBundle;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Recall;
import jakarta.ecofarm_backend.modules.blockchain.facade.BlockchainFacade;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@Path("/v1/block-chain")
@RequireRoles(value = {"ADMIN", "QUALITY_CONTROL", "FARMER"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BlockchainResource {

    @Inject
    private BlockchainFacade blockchainFacade;

    @GET
    @Path("/admin/anchors")
    public List<Anchor> getAllAnchors() {
        return blockchainFacade.getAllAnchors();
    }

    @GET
    @Path("/admin/reconciliation")
    public Response getReconciliation(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("onlyMismatch") boolean onlyMismatch
    ) {
        return Response.ok(blockchainFacade.getReconciliation(page, size, onlyMismatch)).build();
    }

    @GET
    @Path("/batches/{batchId}/anchor-status")
    public AnchorStatus getAnchorStatus(@PathParam("batchId") Long batchId) {
        return blockchainFacade.getAnchorStatus(batchId);
    }

    @POST
    @Path("/admin/anchors/{anchorId}/retry")
    public Anchor retryAnchor(@PathParam("anchorId") Long anchorId) {
        return blockchainFacade.retryAnchor(anchorId);
    }

    @POST
    @Path("/batches/{batchId}/recall")
    public Recall recallBatch(@PathParam("batchId") Long batchId, RecallRequest req) {
        return blockchainFacade.recallBatch(batchId, req);
    }

    @GET
    @Path("/batches/{batchId}/proof-bundle")
    public ProofBundle getProofBundle(@PathParam("batchId") Long batchId) {
        return blockchainFacade.getProofBundle(batchId);
    }

    @POST
    @Path("/admin/outbox/test")
    public OutboxEvent createTestOutbox(@QueryParam("data") @DefaultValue("EcoFarm Polygon Test") String data) {
        return blockchainFacade.createTestOutbox(data);
    }

    @POST
    @Path("/admin/outbox/process")
    public Map<String, Object> triggerProcessOutbox() {
        return blockchainFacade.triggerProcessOutbox();
    }

    @GET
    @Path("/admin/polygon/status")
    public Map<String, Object> testPolygonStatus() throws Exception {
        return blockchainFacade.getPolygonStatus();
    }

    @GET
    @Path("/admin/polygon/confirmations/{txHash}")
    public Map<String, Object> testPolygonConfirmations(@PathParam("txHash") String txHash) throws Exception {
        return blockchainFacade.getPolygonConfirmations(txHash);
    }

    @GET
    @Path("/admin/transactions/{txHash}")
    public Response getTransactionDetail(@PathParam("txHash") String txHash) {
        try {
            return Response.ok(blockchainFacade.getTransactionDetail(txHash)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(java.util.Map.of("error", e.getMessage()))
                    .build();
        }
    }
}
