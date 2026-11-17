package jakarta.ecofarm_backend.modules.quality;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.exception.PermissionDeniedException;
import jakarta.ecofarm_backend.infrastructure.security.UserSecurityDetailsPrincipal;
import jakarta.ecofarm_backend.modules.quality.QualityRequests.HarvestRequest;
import jakarta.ecofarm_backend.modules.quality.QualityRequests.QcRequest;
import jakarta.ecofarm_backend.modules.quality.facade.QualityFacade;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Các endpoint thu hoạch và kiểm định chất lượng cho một lô. Resource này chỉ chuyển đổi HTTP sang
 * {@link QualityFacade}: lấy actor từ security context và map kết quả của facade thành response.
 * Toàn bộ logic nghiệp vụ, khóa dòng, transaction và lưu trữ đều nằm trong facade.
 */
@Path("/v1/batches")
@Produces(MediaType.APPLICATION_JSON)
@RequireRoles({"ADMIN", "ENGINEER", "WORKER"})
public class QualityResource {

    @Inject
    private QualityFacade qualityFacade;

    @Context
    private SecurityContext securityContext;

    @POST
    @Path("/{batchId}/harvest")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response harvest(@PathParam("batchId") Long batchId, HarvestRequest request) {
        boolean engineerOrAdmin = securityContext.isUserInRole("ADMIN") || securityContext.isUserInRole("ENGINEER");
        return Response.status(Response.Status.CREATED)
                .entity(qualityFacade.harvest(batchId, request, requireActorId(), engineerOrAdmin))
                .build();
    }

    @POST
    @Path("/{batchId}/qc")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequireRoles({"ADMIN", "ENGINEER"})
    public Response recordQc(@PathParam("batchId") Long batchId, QcRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(qualityFacade.recordQc(batchId, request, requireActorId()))
                .build();
    }

    @GET
    @Path("/{batchId}/qc")
    public Response listQc(@PathParam("batchId") Long batchId) {
        return Response.ok(qualityFacade.listQc(batchId)).build();
    }

    private Long requireActorId() {
        if (!(securityContext.getUserPrincipal() instanceof UserSecurityDetailsPrincipal user)) {
            throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
        }
        return user.getId();
    }
}
