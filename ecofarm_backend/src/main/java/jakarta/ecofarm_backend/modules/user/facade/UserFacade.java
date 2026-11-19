package jakarta.ecofarm_backend.modules.user.facade;

import jakarta.ecofarm_backend.common.dto.request.UserCriteria;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.common.dto.response.WorkerSummaryResponse;

public interface UserFacade {

    BaseResponse<CursorPageResponse<WorkerSummaryResponse>> findUsers(UserCriteria criteria);
}
