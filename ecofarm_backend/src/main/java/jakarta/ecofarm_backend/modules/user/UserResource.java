package jakarta.ecofarm_backend.modules.user;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.request.UserCriteria;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.common.dto.response.WorkerSummaryResponse;
import jakarta.ecofarm_backend.modules.user.facade.UserFacade;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/v1/users")
@RequireRoles({"ADMIN"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    private UserFacade userFacade;

    @GET
    public BaseResponse<CursorPageResponse<WorkerSummaryResponse>> findUsers(@BeanParam UserCriteria criteria) {
        return userFacade.findUsers(criteria);
    }
}
