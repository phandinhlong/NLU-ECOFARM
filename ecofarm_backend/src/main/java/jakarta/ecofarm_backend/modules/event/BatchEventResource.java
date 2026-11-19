package jakarta.ecofarm_backend.modules.event;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.request.CreateEventRequest;
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
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/v1/batches")
@Tag(name = "BatchEvent", description = "Ghi nhận và tra cứu sự kiện canh tác")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BatchEventResource {

    @Inject
    private EventFacade eventFacade;

    @POST
    @Path("/{id}/events")
    @RequireRoles({"ENGINEER", "FARMER"})
    public Response createEvent(@PathParam("id") @Positive Long id, @Valid CreateEventRequest request) {
        return Response.status(Response.Status.CREATED).entity(BaseResponse.build(eventFacade.createEvent(id, request))).build();
    }

    @GET
    @Path("/{id}/events")
    @RequireRoles({"ENGINEER", "FARMER"})
    public BaseResponse<CursorPageResponse<EventResponse>> findEvents(@PathParam("id") @Positive Long id,
                                                                      @QueryParam("lastIndex") @Positive Long lastIndex,
                                                                      @QueryParam("reviewStatus") EventReviewStatus reviewStatus) {
        return eventFacade.findEvents(id, lastIndex, reviewStatus);
    }
}
