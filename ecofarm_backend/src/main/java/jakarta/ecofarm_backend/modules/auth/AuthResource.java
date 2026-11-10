package jakarta.ecofarm_backend.modules.auth;

import jakarta.ecofarm_backend.common.request.LoginRequest;
import jakarta.ecofarm_backend.common.response.BaseResponse;
import jakarta.ecofarm_backend.common.response.ProfileResponse;
import jakarta.ecofarm_backend.modules.auth.facade.AuthFacade;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/v1/auths")
@Tag(name = "Authentication", description = "Xác thực và phân quyền")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    private AuthFacade authFacade;


    @GET
    @Path("/profile")
    public BaseResponse<ProfileResponse> findProfile(){
        return this.authFacade.findProfile();
    }


    @POST
    @Path("/login")
    @Operation(
            summary = "User login",
            description = "Authenticates the user's credentials and sets the access token and refresh token in HttpOnly cookies."
    )
    public BaseResponse<Boolean> login(@Valid LoginRequest request) {
        return this.authFacade.login(request);
    }

    @POST
    @Path("/refresh-token")
    public BaseResponse<Boolean> refreshToken(){
        return this.authFacade.refreshToken();
    }
}
