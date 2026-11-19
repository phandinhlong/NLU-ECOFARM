package jakarta.ecofarm_backend.modules.event.facade;

import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.common.enums.TaskStatus;
import jakarta.ecofarm_backend.common.dto.response.BatchTaskResponse;
import jakarta.ecofarm_backend.common.dto.request.CompleteTaskRequest;
import jakarta.ecofarm_backend.common.dto.request.CreateTaskRequest;

public interface BatchTaskFacade {
    BaseResponse<BatchTaskResponse> createTask(Long batchId, CreateTaskRequest request);
    BaseResponse<CursorPageResponse<BatchTaskResponse>> findTasks(Long batchId, Long lastIndex, TaskStatus status);
    BaseResponse<BatchTaskResponse> completeTask(Long taskId, CompleteTaskRequest request);
    BaseResponse<Boolean> cancelTask(Long taskId, String reason);
}
