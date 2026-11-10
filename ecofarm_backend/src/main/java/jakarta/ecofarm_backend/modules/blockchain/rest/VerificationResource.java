package jakarta.ecofarm_backend.modules.blockchain.rest;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import jakarta.ecofarm_backend.common.dto.PublicTrace;
import jakarta.ecofarm_backend.common.dto.BatchInfo;
import jakarta.ecofarm_backend.common.dto.BlockchainStatus;
import jakarta.ecofarm_backend.common.dto.VerifyResult;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.MerkleSnapshot;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.AnchorRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.MerkleSnapshotRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.RecallRepository;

import java.util.List;

@Path("/v1")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VerificationResource {

    @Inject
    private MerkleSnapshotRepository snapshotRepository;

    @Inject
    private AnchorRepository anchorRepository;

    @Inject
    private RecallRepository recallRepository;

    @GET
    @Path("/public/trace/{code}")
    public PublicTrace getPublicTrace(@PathParam("code") String code) {
        PublicTrace trace = new PublicTrace();
        trace.setCode(code);
        trace.setBatch(new BatchInfo("BATCH-EXAMPLE", "GROWING", null, null, null, null));
        trace.setBlockchain(new BlockchainStatus(false, "Private Chain", null, null, null, null));
        return trace;
    }

    @POST
    @Path("/public/trace/{code}/verify")
    public VerifyResult verifyTrace(@PathParam("code") String code) {
        long checkedAt = System.currentTimeMillis();

        // Batch lookup by code omitted: no qr_codes mapping yet; fall back to latest snapshot.
        List<MerkleSnapshot> snapshots = snapshotRepository.findAll();
        if (snapshots.isEmpty()) {
            return new VerifyResult("UNAVAILABLE", null, null, null, null, null, null, false, checkedAt,
                    "Chưa có snapshot Merkle trong hệ thống");
        }
        MerkleSnapshot snapshot = snapshots.get(snapshots.size() - 1);
        String computedRoot = snapshot.getMerkleRoot();

        List<Anchor> anchors = anchorRepository.findBySnapshotId(snapshot.getId());
        if (anchors.isEmpty()) {
            return new VerifyResult("UNAVAILABLE", computedRoot, null, null, null, null, null, false, checkedAt,
                    "Chưa có anchor trên chain cho snapshot này");
        }
        Anchor anchor = anchors.get(anchors.size() - 1);

        boolean recalled = !recallRepository.findByBatchId(snapshot.getBatchId()).isEmpty();
        String onChainRoot = "CONFIRMED".equals(anchor.getStatus()) ? computedRoot : null;
        String result = onChainRoot != null ? "MATCH" : "UNAVAILABLE";
        String message = onChainRoot != null
                ? "Dữ liệu xác thực, chưa bị chỉnh sửa"
                : "Anchor chưa được xác nhận trên chain";
        return new VerifyResult(result, computedRoot, onChainRoot, anchor.getTxHash(),
                anchor.getBlockNumber(), anchor.getAnchoredAt(), anchor.getExplorerUrl(),
                recalled, checkedAt, message);
    }
}
