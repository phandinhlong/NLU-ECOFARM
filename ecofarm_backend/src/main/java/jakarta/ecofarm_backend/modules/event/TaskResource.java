package jakarta.ecofarm_backend.modules.event;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.BatchTaskResponse;
import jakarta.ecofarm_backend.common.dto.request.CancelTaskRequest;
import jakarta.ecofarm_backend.common.dto.request.CompleteTaskRequest;
import jakarta.ecofarm_backend.modules.event.facade.BatchTaskFacade;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/v1/tasks")
@Tag(name = "BatchTask", description = "Giao việc và hoàn thành công việc")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TaskResource {

    @Inject
    private BatchTaskFacade batchTaskFacade;

    @POST
    @Path("/{id}/complete")
    @RequireRoles({"FARMER"})
    public BaseResponse<BatchTaskResponse> completeTask(@PathParam("id") @Positive Long id,
                                                        @Valid CompleteTaskRequest request) {
        return batchTaskFacade.completeTask(id, request);
    }

    @POST
    @Path("/{id}/cancel")
    @RequireRoles({"ENGINEER"})
    public BaseResponse<Boolean> cancelTask(@PathParam("id") @Positive Long id,
                                            @Valid CancelTaskRequest request) {
        return batchTaskFacade.cancelTask(id, request.getReason());
    }
}
