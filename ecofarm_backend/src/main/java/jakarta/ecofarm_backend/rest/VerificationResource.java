package jakarta.ecofarm_backend.rest;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import jakarta.ecofarm_backend.service.ReconciliationService;

@Path("/api/v1")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VerificationResource {

    @Inject
    private ReconciliationService reconciliationService;

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
        // Compute vs on-chain root
        VerifyResult result = new VerifyResult("MISMATCH", null, null, null, null, false, System.currentTimeMillis());
        return result;
    }

    // DTOs
    public static class PublicTrace {
        String code;
        BatchInfo batch;
        BlockchainStatus blockchain;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public BatchInfo getBatch() { return batch; }
        public void setBatch(BatchInfo batch) { this.batch = batch; }
        public BlockchainStatus getBlockchain() { return blockchain; }
        public void setBlockchain(BlockchainStatus blockchain) { this.blockchain = blockchain; }
    }

    public static class BatchInfo {
        String batchCode;
        String status;
        String varietyName;
        String greenhouseCode;
        String greenhouseName;
        String sowDate;

        public BatchInfo(String batchCode, String status, String vName, String gCode, String gName, String sDate) {
            this.batchCode = batchCode;
            this.status = status;
            this.varietyName = vName;
            this.greenhouseCode = gCode;
            this.greenhouseName = gName;
            this.sowDate = sDate;
        }
    }

    public static class BlockchainStatus {
        boolean anchored;
        String chainName;
        String merkleRoot;
        String txHash;
        Long anchoredAt;
        String explorerUrl;

        public BlockchainStatus(boolean anchored, String chain, String mr, String tx, Long att, String exp) {
            this.anchored = anchored;
            this.chainName = chain;
            this.merkleRoot = mr;
            this.txHash = tx;
            this.anchoredAt = att;
            this.explorerUrl = exp;
        }
    }

    public static class VerifyResult {
        String result;
        String computedRoot;
        String onChainRoot;
        String txHash;
        Long blockNumber;
        Long anchoredAt;
        boolean recalled;
        long checkedAt;

        public VerifyResult(String result, String cr, String ocr, String tx, Long bn, Boolean rec, long ct) {
            this.result = result;
            this.computedRoot = cr;
            this.onChainRoot = ocr;
            this.txHash = tx;
            this.blockNumber = bn;
            this.anchoredAt = anchoredAt;
            this.recalled = rec;
            this.checkedAt = ct;
        }

        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
        public String getComputedRoot() { return computedRoot; }
        public void setComputedRoot(String computedRoot) { this.computedRoot = computedRoot; }
        public String getOnChainRoot() { return onChainRoot; }
        public void setOnChainRoot(String onChainRoot) { this.onChainRoot = onChainRoot; }
        public String getTxHash() { return txHash; }
        public void setTxHash(String txHash) { this.txHash = txHash; }
        public Long getBlockNumber() { return blockNumber; }
        public void setBlockNumber(Long blockNumber) { this.blockNumber = blockNumber; }
        public Long getAnchoredAt() { return anchoredAt; }
        public void setAnchoredAt(Long anchoredAt) { this.anchoredAt = anchoredAt; }
        public boolean isRecalled() { return recalled; }
        public void setRecalled(boolean recalled) { this.recalled = recalled; }
        public long getCheckedAt() { return checkedAt; }
        public void setCheckedAt(long checkedAt) { this.checkedAt = checkedAt; }
    }
}
