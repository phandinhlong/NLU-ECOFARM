package jakarta.ecofarm_backend.modules.batch;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.request.PageCriteria;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.PageResponse;
import jakarta.ecofarm_backend.common.dto.request.AssignWorkersRequest;
import jakarta.ecofarm_backend.common.dto.response.BatchDetailResponse;
import jakarta.ecofarm_backend.common.dto.response.BatchListItemResponse;
import jakarta.ecofarm_backend.common.dto.request.CreateBatchRequest;
import jakarta.ecofarm_backend.common.dto.response.WorkerSummaryResponse;
import jakarta.ecofarm_backend.modules.batch.facade.BatchFacade;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/v1/batches")
@Tag(name = "Batch", description = "Quản lý lô canh tác")
@RequireRoles(value = {"ADMIN", "ENGINEER", "QUALITY_CONTROL"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BatchResource {

    @Inject
    private BatchFacade batchFacade;

    @GET
    public BaseResponse<PageResponse<BatchListItemResponse>> findBatchesByCriteria(@BeanParam PageCriteria criteria) {
        return batchFacade.findBatchesByCriteria(criteria);
    }

    @GET
    @Path("/workers")
    public BaseResponse<List<WorkerSummaryResponse>> listWorkers() {
        return batchFacade.listWorkers();
    }

    @GET
    @Path("/{id}")
    public BaseResponse<BatchDetailResponse> detail(@PathParam("id") Long id) {
        return batchFacade.detail(id);
    }

    @GET
    @Path("/{id}/workers")
    public BaseResponse<List<WorkerSummaryResponse>> listAssignedWorkers(@PathParam("id") Long id) {
        return batchFacade.listAssignedWorkers(id);
    }

    @PUT
    @Path("/{id}/workers")
    public BaseResponse<List<WorkerSummaryResponse>> assignWorkers(
            @PathParam("id") Long id,
            @Valid AssignWorkersRequest request) {
        return batchFacade.assignWorkers(id, request);
    }

    @POST
    public Response create(@Valid CreateBatchRequest request) {
        return Response.status(Response.Status.CREATED).entity(batchFacade.create(request)).build();
    }
}
