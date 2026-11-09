package jakarta.ecofarm_backend.rest;

import jakarta.ejb.Stateless;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import jakarta.ecofarm_backend.dto.PublicTrace;
import jakarta.ecofarm_backend.dto.BatchInfo;
import jakarta.ecofarm_backend.dto.BlockchainStatus;
import jakarta.ecofarm_backend.dto.VerifyResult;

@Path("/api/v1")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VerificationResource {

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
        VerifyResult result = new VerifyResult("MISMATCH", null, null, null, null, null, false, System.currentTimeMillis());
        return result;
    }
}
