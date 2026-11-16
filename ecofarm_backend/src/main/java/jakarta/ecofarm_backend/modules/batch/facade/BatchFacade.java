package jakarta.ecofarm_backend.modules.batch.facade;

import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.PageResponse;
import jakarta.ecofarm_backend.common.dto.request.AssignWorkersRequest;
import jakarta.ecofarm_backend.common.dto.response.BatchDetailResponse;
import jakarta.ecofarm_backend.common.dto.response.BatchListItemResponse;
import jakarta.ecofarm_backend.common.dto.request.CreateBatchRequest;
import jakarta.ecofarm_backend.common.dto.response.WorkerSummaryResponse;

import java.util.List;

public interface BatchFacade {

    BaseResponse<PageResponse<BatchListItemResponse>> list(int page, int size, String status,
            Long greenhouseId, String q, boolean mine);

    BaseResponse<BatchDetailResponse> detail(Long batchId);

    BaseResponse<BatchDetailResponse> create(CreateBatchRequest request);

    BaseResponse<List<WorkerSummaryResponse>> listWorkers();

    BaseResponse<List<WorkerSummaryResponse>> listAssignedWorkers(Long batchId);

    BaseResponse<List<WorkerSummaryResponse>> assignWorkers(Long batchId, AssignWorkersRequest request);
}
