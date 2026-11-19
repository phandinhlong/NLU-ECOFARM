package jakarta.ecofarm_backend.modules.batch;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.request.PageCriteria;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.common.dto.request.AssignWorkersRequest;
import jakarta.ecofarm_backend.common.dto.request.CreateBatchRequest;
import jakarta.ecofarm_backend.common.dto.request.HarvestRequest;
import jakarta.ecofarm_backend.common.dto.request.RecordQcRequest;
import jakarta.ecofarm_backend.common.dto.request.ReviewQcRequest;
import jakarta.ecofarm_backend.common.dto.response.BatchDetailResponse;
import jakarta.ecofarm_backend.common.dto.response.BatchListItemResponse;
import jakarta.ecofarm_backend.common.dto.request.CreateBatchRequest;
import jakarta.ecofarm_backend.common.dto.request.UpdateBatchRequest;
import jakarta.ecofarm_backend.common.dto.response.WorkerSummaryResponse;
import jakarta.ecofarm_backend.common.enums.RoleEnum;
import jakarta.ecofarm_backend.modules.batch.facade.BatchFacade;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/v1/batches")
@Tag(name = "Batch", description = "Quản lý lô canh tác")
@RequireRoles({"ENGINEER"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BatchResource {

    @Inject
    private BatchFacade batchFacade;

    @GET
    @RequireRoles({"ENGINEER", "FARMER"})
    public BaseResponse<CursorPageResponse<BatchListItemResponse>> findBatchesByCriteria(@BeanParam PageCriteria criteria) {
        return batchFacade.findBatchesByCriteria(criteria);
    }

    @GET
    @Path("/workers")
    public BaseResponse<CursorPageResponse<WorkerSummaryResponse>> findBatchWorkers(@QueryParam("lastIndex") Long lastIndex) {
        return batchFacade.findBatchWorkers(lastIndex);
    }

    @GET
    @Path("/{id}")
    @RequireRoles({"ENGINEER", "FARMER"})
    public BaseResponse<BatchDetailResponse> findBatchDetail(@PathParam("id") Long id) {
        return batchFacade.findBatchDetail(id);
    }

    @GET
    @Path("/{id}/workers")
    @RequireRoles({"ENGINEER", "FARMER"})
    public BaseResponse<CursorPageResponse<WorkerSummaryResponse>> findAssignedWorkersByBatchId(@PathParam("id") Long id, @QueryParam("lastIndex") Long lastIndex) {
        return batchFacade.findAssignedWorkersByBatchId(id, lastIndex);
    }

    @POST
    @Path("/{id}/workers")
    public BaseResponse<Boolean> addWorkers(@PathParam("id") Long id, @Valid AssignWorkersRequest request) {
        return batchFacade.addWorkers(id, request);
    }

    @DELETE
    @Path("/{id}/workers")
    public BaseResponse<Boolean> removeWorkers(@PathParam("id") Long id, @Valid AssignWorkersRequest request) {
        return batchFacade.removeWorkers(id, request);
    }

    @POST
    public Response createBatch(@Valid CreateBatchRequest request) {
        return Response.status(Response.Status.CREATED).entity(batchFacade.createBatch(request)).build();
    }

    @PATCH
    @Path("/{id}")
    public BaseResponse<BatchDetailResponse> updateBatch(@PathParam("id") @Positive Long id, @Valid UpdateBatchRequest request) {
        return batchFacade.updateBatch(id, request);
    }

    @DELETE
    @Path("/{id}")
    public BaseResponse<Boolean> deleteBatch(@PathParam("id") @Positive Long id) {
        return batchFacade.deleteBatch(id);
    }

    @POST
    @Path("/{id}/qc")
    public BaseResponse<QcResponse> recordQc(@PathParam("id") @Positive Long id, @Valid RecordQcRequest request) {
        return batchFacade.recordQc(id, request);
    }

    @GET
    @Path("/{id}/qc")
    public BaseResponse<QcResponse> getLatestQc(@PathParam("id") @Positive Long id) {
        return batchFacade.getLatestQc(id);
    }

    @POST
    @Path("/{id}/qc/review")
    @RequireRoles({"ADMIN"})
    public BaseResponse<QcResponse> reviewQc(@PathParam("id") @Positive Long id, @Valid ReviewQcRequest request) {
        return batchFacade.reviewQc(id, request);
    }

    @POST
    @Path("/{id}/harvest")
    @RequireRoles({"ADMIN", "ENGINEER", "FARMER"})
    public BaseResponse<HarvestResponse> harvestBatch(@PathParam("id") @Positive Long id, @Valid HarvestRequest request) {
        return batchFacade.harvestBatch(id, request);
    }
}
