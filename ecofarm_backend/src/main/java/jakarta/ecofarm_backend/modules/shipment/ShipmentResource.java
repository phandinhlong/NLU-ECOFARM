package jakarta.ecofarm_backend.modules.shipment;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.request.ShipmentCreateRequest;
import jakarta.ecofarm_backend.common.dto.request.ShipmentReceiveRequest;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.ShipmentResponse;
import jakarta.ecofarm_backend.modules.shipment.facade.ShipmentFacade;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/v1")
@Tag(name = "Shipping", description = "Xuất kho và xác nhận nhận hàng")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ShipmentResource {

    @Inject
    private ShipmentFacade shipmentFacade;

    @POST
    @Path("/batches/{batchId}/shipments")
    @RequireRoles({"PACKER", "ADMIN"})
    @Operation(summary = "Xuất kho", description = "Chuyển lô ACTIVE sang SHIPPED")
    public Response createShipment(@PathParam("batchId") Long batchId, @Valid ShipmentCreateRequest request) {
        return Response.status(Response.Status.CREATED).entity(shipmentFacade.createShipment(batchId, request)).build();
    }

    @POST
    @Path("/shipments/{shipmentId}/receive")
    @RequireRoles({"PARTNER_QC", "ADMIN"})
    @Operation(summary = "Xác nhận nhận hàng", description = "RECEIVED hoặc REJECTED; từ chối thì trả lô về ACTIVE")
    public BaseResponse<ShipmentResponse> receiveShipment(@PathParam("shipmentId") Long shipmentId, @Valid ShipmentReceiveRequest request) {
        return shipmentFacade.receiveShipment(shipmentId, request);
    }

    @GET
    @Path("/batches/{batchId}/shipments")
    @RequireRoles({"PACKER", "ADMIN", "PARTNER_QC"})
    @Operation(summary = "Danh sách phiếu xuất kho theo lô")
    public BaseResponse<List<ShipmentResponse>> findShipmentsByBatch(@PathParam("batchId") Long batchId) {
        return shipmentFacade.findShipmentsByBatch(batchId);
    }

    @GET
    @Path("/shipments")
    @RequireRoles({"PACKER", "ADMIN", "PARTNER_QC"})
    @Operation(summary = "Danh sách phiếu xuất kho theo trạng thái")
    public BaseResponse<List<ShipmentResponse>> findShipmentsByStatus(@QueryParam("status") String status) {
        return shipmentFacade.findShipmentsByStatus(status);
    }
}
