package jakarta.ecofarm_backend.modules.auth;

import jakarta.ecofarm_backend.common.constant.SecurityConstant;
import jakarta.ecofarm_backend.common.dto.request.LoginRequest;
import jakarta.ecofarm_backend.common.dto.request.SignupRequest;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.ProfileResponse;
import jakarta.ecofarm_backend.common.util.CookieBuilderUtil;
import jakarta.ecofarm_backend.modules.auth.facade.AuthFacade;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.Map;

@Path("/v1/auths")
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
    public BaseResponse<ProfileResponse> findProfile(@Context SecurityContext securityContext) {
        return this.authFacade.findProfile(securityContext.getUserPrincipal().getName());
    }

    @POST
    @Path("/login")
    @Operation(
            summary = "User login",
            description = "Authenticates the user's credentials and sets the access token and refresh token in HttpOnly cookies."
    )
    public Response login(@Valid LoginRequest request) {
        BaseResponse<Map<String, String>> result = this.authFacade.login(request);
        Map<String, String> tokens = result.getData();

        return Response.ok(result)
                .header("Set-Cookie", cookieBuilder.buildCookie(
                        SecurityConstant.ACCESS_TOKEN_COOKIE_NAME, tokens.get("accessToken"), "/", accessMaxAge))
                .header("Set-Cookie", cookieBuilder.buildCookie(
                        SecurityConstant.REFRESH_TOKEN_COOKIE_NAME, tokens.get("refreshToken"), "/", refreshMaxAge))
                .build();
    }

    @POST
    @Path("/refresh-token")
    public Response refreshToken(@CookieParam(SecurityConstant.REFRESH_TOKEN_COOKIE_NAME) String refreshToken) {
        BaseResponse<Map<String, String>> result = this.authFacade.refreshTokens(refreshToken);

        return Response.ok(result)
                .header("Set-Cookie", cookieBuilder.buildCookie(
                        SecurityConstant.ACCESS_TOKEN_COOKIE_NAME, result.getData().get("accessToken"), "/", accessMaxAge))
                .build();
    }

    @POST
    @Path("/sign-up")
    public BaseResponse<Boolean> signup(@Valid SignupRequest request) {
        return this.authFacade.signup(request);
    }

    @GET
    @Path("/activation")
    public Response activateUser(@QueryParam("token") String token) {
        URI location = this.authFacade.activateUser(token);
        return Response.seeOther(location).build();
    }
}
