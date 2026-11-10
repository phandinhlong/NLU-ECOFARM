package jakarta.ecofarm_backend.modules.auth;

import jakarta.ecofarm_backend.common.dto.request.ForgotPasswordRequest;
import jakarta.ecofarm_backend.common.dto.request.LoginRequest;
import jakarta.ecofarm_backend.common.dto.request.SignupRequest;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.ProfileResponse;
import jakarta.ecofarm_backend.modules.auth.facade.AuthFacade;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;


@Path("api/v1/auths")
@Tag(name = "Authentication", description = "Xác thực và phân quyền")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    private AuthFacade authFacade;

    @Inject
    private CookieBuilderUtil cookieBuilder;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.cookie.access-max-age", defaultValue = "900")
    private Integer accessMaxAge;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.cookie.refresh-max-age", defaultValue = "604800")
    private Integer refreshMaxAge;

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

    @POST
    @Path("/sign-up")
    public BaseResponse<Boolean> signup(@Valid SignupRequest request){
        return this.authFacade.signup(request);
    }


    @GET
    @Path("/activation")
    public Response activateUser(@QueryParam("token") String token){
        URI location = this.authFacade.activateUser(token);
        return Response.seeOther(location).build();
    }

    @POST
    @Path("/forgot-password")
    public BaseResponse<Boolean> forgotPassword(@Valid ForgotPasswordRequest request){
        return this.authFacade.forgotPassword(request);
    }


}
