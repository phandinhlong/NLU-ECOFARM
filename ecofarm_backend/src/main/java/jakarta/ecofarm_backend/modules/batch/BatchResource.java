package jakarta.ecofarm_backend.modules.batch;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.PageResponse;
import jakarta.ecofarm_backend.modules.batch.dto.AssignWorkersRequest;
import jakarta.ecofarm_backend.modules.batch.dto.BatchDetailResponse;
import jakarta.ecofarm_backend.modules.batch.dto.BatchListItemResponse;
import jakarta.ecofarm_backend.modules.batch.dto.CreateBatchRequest;
import jakarta.ecofarm_backend.modules.batch.dto.WorkerSummaryResponse;
import jakarta.ecofarm_backend.modules.batch.facade.BatchFacade;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/v1/batches")
@Tag(name = "Batch", description = "Quản lý lô canh tác")
@RequireRoles(value = {"ADMIN", "ENGINEER", "FARMER", "QUALITY_CONTROL", "PACKER"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BatchResource {

    @Inject
    private BatchFacade batchFacade;

    @GET
    public BaseResponse<PageResponse<BatchListItemResponse>> list(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("status") String status,
            @QueryParam("greenhouseId") Long greenhouseId,
            @QueryParam("q") String q,
            @QueryParam("mine") @DefaultValue("false") boolean mine) {
        return batchFacade.list(page, size, status, greenhouseId, q, mine);
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
