package jakarta.ecofarm_backend.modules.ver.rest;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.ProofBundleResponse;
import jakarta.ecofarm_backend.modules.ver.facade.TraceFacade;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/v1/ver")
@Tag(name = "Verification", description = "Gói bằng chứng xác thực cho QC và Thanh tra")
@RequireRoles({"QUALITY_CONTROL", "ADMIN"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProofBundleResource {

    @Inject
    private TraceFacade traceFacade;

    @GET
    @Path("/proof-bundle/{code}")
    @Operation(
            summary = "Gói bằng chứng Merkle của mã truy xuất",
            description = "Trả về salt, leaves, Merkle proof và anchor tx để kiểm chứng độc lập (FR-VER-006)."
    )
    public BaseResponse<ProofBundleResponse> getProofBundle(@PathParam("code") String code) {
        return traceFacade.getProofBundle(code);
    }
}
