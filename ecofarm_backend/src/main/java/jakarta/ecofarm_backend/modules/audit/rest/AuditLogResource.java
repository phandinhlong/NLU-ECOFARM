package jakarta.ecofarm_backend.modules.audit.rest;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.request.AuditLogCriteria;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.common.dto.response.AuditLogResponse;
import jakarta.ecofarm_backend.modules.audit.facade.AuditLogFacade;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/v1/audit-logs")
@RequireRoles({"ADMIN"})
@Produces(MediaType.APPLICATION_JSON)
public class AuditLogResource {

    @Inject
    private AuditLogFacade auditLogFacade;

    @GET
    public BaseResponse<CursorPageResponse<AuditLogResponse>> findAuditLogs(@Valid @BeanParam AuditLogCriteria criteria) {
        return auditLogFacade.findAuditLogs(criteria);
    }

    @GET
    @Path("/statistics")
    public BaseResponse<Long> countAuditLogs(@Valid @BeanParam AuditLogCriteria criteria) {
        return auditLogFacade.countAuditLogs(criteria);
    }
}
