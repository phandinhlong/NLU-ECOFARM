package jakarta.ecofarm_backend.modules.event;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.request.AmendEventRequest;
import jakarta.ecofarm_backend.common.dto.request.ReassignApproverRequest;
import jakarta.ecofarm_backend.common.dto.request.RejectEventRequest;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.common.dto.response.EventResponse;
import jakarta.ecofarm_backend.common.enums.EventReviewStatus;
import jakarta.ecofarm_backend.modules.event.facade.EventFacade;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/v1/events")
@Tag(name = "EventReview", description = "Duyệt và đính chính sự kiện canh tác")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventReviewResource {

    @Inject
    private EventFacade eventFacade;

    @GET
    @RequireRoles({"ENGINEER"})
    public BaseResponse<CursorPageResponse<EventResponse>> findPending(@QueryParam("lastIndex") @Positive Long lastIndex,
                                                                      @QueryParam("greenhouseId") @Positive Long greenhouseId,
                                                                      @QueryParam("reviewStatus") EventReviewStatus reviewStatus) {
        return eventFacade.findReviewQueue(lastIndex, greenhouseId, reviewStatus);
    }

    @GET
    @Path("/review-count")
    @RequireRoles({"ENGINEER"})
    public BaseResponse<Long> countReview(@QueryParam("greenhouseId") @Positive Long greenhouseId,
                                          @QueryParam("reviewStatus") EventReviewStatus reviewStatus) {
        return eventFacade.countReviewQueue(greenhouseId, reviewStatus);
    }

    @GET
    @Path("/{id}")
    @RequireRoles({"ENGINEER", "FARMER"})
    public BaseResponse<EventResponse> findById(@PathParam("id") @Positive Long id) {
        return eventFacade.findEvent(id);
    }

    @POST
    @Path("/{id}/approve")
    @RequireRoles({"ENGINEER"})
    public BaseResponse<EventResponse> approve(@PathParam("id") @Positive Long id) {
        return eventFacade.approve(id);
    }

    @POST
    @Path("/{id}/reject")
    @RequireRoles({"ENGINEER"})
    public BaseResponse<EventResponse> reject(@PathParam("id") @Positive Long id, @Valid RejectEventRequest request) {
        return eventFacade.reject(id, request);
    }

    @POST
    @Path("/{id}/reassign-approver")
    @RequireRoles({"ADMIN"})
    public BaseResponse<EventResponse> reassignApprover(@PathParam("id") @Positive Long id, @Valid ReassignApproverRequest request) {
        return eventFacade.reassignApprover(id, request);
    }

    @POST
    @Path("/{id}/amend")
    @RequireRoles({"ENGINEER"})
    public BaseResponse<EventResponse> amend(@PathParam("id") @Positive Long id, @Valid AmendEventRequest request) {
        return eventFacade.amend(id, request);
    }
}
