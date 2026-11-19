package jakarta.ecofarm_backend.modules.batch.facade;

import jakarta.ecofarm_backend.common.dto.request.AssignWorkersRequest;
import jakarta.ecofarm_backend.common.dto.request.CreateBatchRequest;
import jakarta.ecofarm_backend.common.dto.request.HarvestRequest;
import jakarta.ecofarm_backend.common.dto.request.PageCriteria;
import jakarta.ecofarm_backend.common.dto.request.RecordQcRequest;
import jakarta.ecofarm_backend.common.dto.request.ReviewQcRequest;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.common.dto.response.BatchDetailResponse;
import jakarta.ecofarm_backend.common.dto.response.BatchListItemResponse;
import jakarta.ecofarm_backend.common.dto.response.HarvestResponse;
import jakarta.ecofarm_backend.common.dto.response.QcResponse;
import jakarta.ecofarm_backend.common.dto.request.UpdateBatchRequest;
import jakarta.ecofarm_backend.common.dto.response.WorkerSummaryResponse;

public interface BatchFacade {

    BaseResponse<CursorPageResponse<BatchListItemResponse>> findBatchesByCriteria(PageCriteria criteria);

    BaseResponse<BatchDetailResponse> findBatchDetail(Long batchId);

    BaseResponse<BatchDetailResponse> createBatch(CreateBatchRequest request);

    BaseResponse<BatchDetailResponse> updateBatch(Long batchId, UpdateBatchRequest request);

    BaseResponse<Boolean> deleteBatch(Long batchId);

    BaseResponse<CursorPageResponse<WorkerSummaryResponse>> findBatchWorkers(Long lastIndex);

    BaseResponse<CursorPageResponse<WorkerSummaryResponse>> findAssignedWorkersByBatchId(Long batchId, Long lastIndex);

    BaseResponse<Boolean> addWorkers(Long batchId, AssignWorkersRequest request);

    BaseResponse<Boolean> removeWorkers(Long batchId, AssignWorkersRequest request);

    BaseResponse<QcResponse> recordQc(Long batchId, RecordQcRequest request);

    BaseResponse<QcResponse> reviewQc(Long batchId, ReviewQcRequest request);

    BaseResponse<QcResponse> getLatestQc(Long batchId);

    BaseResponse<HarvestResponse> harvestBatch(Long batchId, HarvestRequest request);
}
