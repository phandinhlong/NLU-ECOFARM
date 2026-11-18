package jakarta.ecofarm_backend.modules.shipment.facade;

import jakarta.ecofarm_backend.common.dto.request.ShipmentCreateRequest;
import jakarta.ecofarm_backend.common.dto.request.ShipmentReceiveRequest;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.ShipmentEligibleBatchResponse;
import jakarta.ecofarm_backend.common.dto.response.ShipmentResponse;

import java.util.List;

public interface ShipmentFacade {

    BaseResponse<ShipmentResponse> createShipment(Long batchId, ShipmentCreateRequest request);

    BaseResponse<ShipmentResponse> receiveShipment(Long shipmentId, ShipmentReceiveRequest request);

    BaseResponse<List<ShipmentEligibleBatchResponse>> findEligibleBatches();

    BaseResponse<List<ShipmentResponse>> findShipmentsByBatch(Long batchId);

    BaseResponse<List<ShipmentResponse>> findShipmentsByStatus(String status);
}
