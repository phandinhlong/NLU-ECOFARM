package jakarta.ecofarm_backend.modules.ver.rest;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.modules.ver.dto.ProofBundleResponse;
import jakarta.ecofarm_backend.modules.ver.facade.TraceFacade;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/v1/ver")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProofBundleResource {

    @Inject
    private TraceFacade traceFacade;

    @GET
    @Path("/proof-bundle/{code}")
    @RequireRoles({"QUALITY_CONTROL", "ADMIN"})
    public ProofBundleResponse getProofBundle(@PathParam("code") String code) {
        return traceFacade.getProofBundle(code);
    }
}
