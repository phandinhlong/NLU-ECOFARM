package jakarta.ecofarm_backend.modules.event;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.common.enums.TaskStatus;
import jakarta.ecofarm_backend.common.dto.response.BatchTaskResponse;
import jakarta.ecofarm_backend.common.dto.request.CreateTaskRequest;
import jakarta.ecofarm_backend.modules.event.facade.BatchTaskFacade;
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
@Tag(name = "BatchTask", description = "Giao việc và hoàn thành công việc")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BatchTaskResource {

    @Inject
    private BatchTaskFacade batchTaskFacade;

    @POST
    @Path("/{id}/tasks")
    @RequireRoles({"ENGINEER"})
    public Response createTask(@PathParam("id") @Positive Long id, @Valid CreateTaskRequest request) {
        return Response.status(Response.Status.CREATED).entity(batchTaskFacade.createTask(id, request)).build();
    }

    @GET
    @Path("/{id}/tasks")
    @RequireRoles({"ENGINEER", "FARMER"})
    public BaseResponse<CursorPageResponse<BatchTaskResponse>> findTasks(@PathParam("id") @Positive Long id,
                                                                        @QueryParam("lastIndex") @Positive Long lastIndex,
                                                                        @QueryParam("status") TaskStatus status) {
        return batchTaskFacade.findTasks(id, lastIndex, status);
    }
}
