package jakarta.ecofarm_backend.modules.ver.rest;

import jakarta.ecofarm_backend.common.dto.response.PublicTrace;
import jakarta.ecofarm_backend.common.dto.response.VerifyResult;
import jakarta.ecofarm_backend.modules.ver.facade.TraceFacade;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Khong @Consumes(JSON) de POST verify khong can body (FE goi khong kem Content-Type)
@Path("/trace")
@Produces(MediaType.APPLICATION_JSON)
public class PublicTraceResource {

    @Inject
    private TraceFacade traceFacade;

    @GET
    @Path("/{code}")
    public PublicTrace getTrace(@PathParam("code") String code) {
        return traceFacade.getTrace(code);
    }

    @POST
    @Path("/{code}/verify")
    public VerifyResult verifyTrace(@PathParam("code") String code) {
        return traceFacade.verifyTrace(code);
    }
}
