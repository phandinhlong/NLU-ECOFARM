package jakarta.ecofarm_backend.modules.blockchain;

import jakarta.ecofarm_backend.common.dto.response.PublicTrace;
import jakarta.ecofarm_backend.common.dto.response.VerifyResult;
import jakarta.ecofarm_backend.modules.blockchain.facade.VerificationFacade;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

@Path("/v1/block-chain")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VerificationResource {

    @Inject
    private VerificationFacade verificationFacade;

    @Context
    private HttpServletRequest httpRequest;

    @GET
    @Path("/public/trace/{code}")
    public PublicTrace getPublicTrace(@PathParam("code") String code) {
        String clientIp = resolveClientIp();
        String userAgent = httpRequest != null ? httpRequest.getHeader("User-Agent") : null;
        return verificationFacade.getPublicTrace(code, clientIp, userAgent);
    }

    private String resolveClientIp() {
        if (httpRequest == null) return null;
        String forwarded = httpRequest.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return httpRequest.getRemoteAddr();
    }

    @POST
    @Path("/public/trace/{code}/verify")
    public VerifyResult verifyTrace(@PathParam("code") String code) {
        return verificationFacade.verifyTrace(code);
    }
}
