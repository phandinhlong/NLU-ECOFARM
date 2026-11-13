package jakarta.ecofarm_backend.modules.ver.rest;

import jakarta.ecofarm_backend.modules.ver.dto.TraceResponse;
import jakarta.ecofarm_backend.modules.ver.dto.VerVerifyResponse;
import jakarta.ecofarm_backend.modules.ver.facade.TraceFacade;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/trace")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublicTraceResource {

    @Inject
    private TraceFacade traceFacade;

    @GET
    @Path("/{code}")
    public TraceResponse getTrace(@PathParam("code") String code) {
        return traceFacade.getTrace(code);
    }

    @POST
    @Path("/{code}/verify")
    public VerVerifyResponse verifyTrace(@PathParam("code") String code) {
        return traceFacade.verifyTrace(code);
    }
}
