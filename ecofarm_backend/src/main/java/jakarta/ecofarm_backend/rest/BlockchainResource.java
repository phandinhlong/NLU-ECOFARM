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
    public List<ReconciliationService.ReconciliationItem> getReconciliation(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("onlyMismatch") @DefaultValue("false") boolean onlyMismatch
    ) {
        List<ReconciliationService.ReconciliationItem> items = reconciliationService.reconcileAll();
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

    public static class AnchorStatus {
        Long batchId;
        String batchStatus;
        PrivateChainStatus privateChain;
        PublicChainStatus publicChain;
        Integer attempts;
        String lastError;
        Long nextAttemptAt;

        public Long getBatchId() { return batchId; }
        public void setBatchId(Long batchId) { this.batchId = batchId; }
        public String getBatchStatus() { return batchStatus; }
        public void setBatchStatus(String batchStatus) { this.batchStatus = batchStatus; }
        public PrivateChainStatus getPrivateChain() { return privateChain; }
        public void setPrivateChain(PrivateChainStatus privateChain) { this.privateChain = privateChain; }
        public PublicChainStatus getPublicChain() { return publicChain; }
        public void setPublicChain(PublicChainStatus publicChain) { this.publicChain = publicChain; }
        public Integer getAttempts() { return attempts; }
        public void setAttempts(Integer attempts) { this.attempts = attempts; }
        public String getLastError() { return lastError; }
        public void setLastError(String lastError) { this.lastError = lastError; }
        public Long getNextAttemptAt() { return nextAttemptAt; }
        public void setNextAttemptAt(Long nextAttemptAt) { this.nextAttemptAt = nextAttemptAt; }
    }

    public static class PrivateChainStatus {
        boolean finalized;
        String txHash;

        public PrivateChainStatus(boolean finalized, String txHash) {
            this.finalized = finalized;
            this.txHash = txHash;
        }

        public boolean isFinalized() { return finalized; }
        public void setFinalized(boolean finalized) { this.finalized = finalized; }
        public String getTxHash() { return txHash; }
        public void setTxHash(String txHash) { this.txHash = txHash; }
    }

    public static class PublicChainStatus {
        String txHash;
        Long blockNumber;
        int confirmations;
        String status;
        String explorerUrl;

        public String getTxHash() { return txHash; }
        public void setTxHash(String txHash) { this.txHash = txHash; }
        public Long getBlockNumber() { return blockNumber; }
        public void setBlockNumber(Long blockNumber) { this.blockNumber = blockNumber; }
        public int getConfirmations() { return confirmations; }
        public void setConfirmations(int confirmations) { this.confirmations = confirmations; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getExplorerUrl() { return explorerUrl; }
        public void setExplorerUrl(String explorerUrl) { this.explorerUrl = explorerUrl; }
    }
}
