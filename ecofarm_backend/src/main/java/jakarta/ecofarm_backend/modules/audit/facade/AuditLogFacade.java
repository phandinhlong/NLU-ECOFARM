package jakarta.ecofarm_backend.modules.audit.facade;

import jakarta.ecofarm_backend.common.dto.request.AuditLogCriteria;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.modules.audit.dto.AuditLogResponse;

public interface AuditLogFacade {
    BaseResponse<CursorPageResponse<AuditLogResponse>> findAuditLogs(AuditLogCriteria criteria);

    BaseResponse<Long> countAuditLogs(AuditLogCriteria criteria);
}
