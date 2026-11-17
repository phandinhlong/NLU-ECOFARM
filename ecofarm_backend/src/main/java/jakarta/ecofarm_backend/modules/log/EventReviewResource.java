package jakarta.ecofarm_backend.modules.log;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.exception.PermissionDeniedException;
import jakarta.ecofarm_backend.infrastructure.security.UserSecurityDetailsPrincipal;
import jakarta.ecofarm_backend.modules.log.EventRequests.AmendRequest;
import jakarta.ecofarm_backend.modules.log.EventRequests.RejectRequest;
import jakarta.ecofarm_backend.modules.log.facade.EventReviewFacade;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Các endpoint duyệt của kỹ sư cho một sự kiện đã ghi, gốc tại {@code /v1/events/{eventId}} theo hợp
 * đồng API (tách khỏi endpoint tạo theo lô). Resource này chỉ chuyển đổi HTTP sang
 * {@link EventReviewFacade}: lấy actor/vai trò từ security context và map kết quả thành response.
 * Toàn bộ logic nghiệp vụ, khóa dòng và transaction nằm trong facade.
 */
@Path("/v1/events/{eventId}")
@Produces(MediaType.APPLICATION_JSON)
@RequireRoles({"ADMIN", "ENGINEER", "WORKER", "FARMER", "PACKER", "REGULATOR"})
public class EventReviewResource {

    @Inject
    private EventReviewFacade eventReviewFacade;

    @Context
    private SecurityContext securityContext;

    @POST
    @Path("/approve")
    public Response approve(@PathParam("eventId") Long eventId) {
        return Response.ok(eventReviewFacade.approve(eventId, engineerOrAdmin())).build();
    }

    @POST
    @Path("/reject")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reject(@PathParam("eventId") Long eventId, RejectRequest request) {
        return Response.ok(eventReviewFacade.reject(eventId, request, engineerOrAdmin())).build();
    }

    @POST
    @Path("/amend")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response amend(@PathParam("eventId") Long eventId, AmendRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(eventReviewFacade.amend(eventId, request, requireActorId(), engineerOrAdmin()))
                .build();
    }

    private Long requireActorId() {
        if (!(securityContext.getUserPrincipal() instanceof UserSecurityDetailsPrincipal user)) {
            throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
        }
        return user.getId();
    }

    private boolean engineerOrAdmin() {
        return securityContext.isUserInRole("ADMIN") || securityContext.isUserInRole("ENGINEER");
    }
}
