package jakarta.ecofarm_backend.modules.auth.facade;

import jakarta.ecofarm_backend.common.dto.request.ForgotPasswordRequest;
import jakarta.ecofarm_backend.common.dto.request.LoginRequest;
import jakarta.ecofarm_backend.common.dto.request.SignupRequest;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.ProfileResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.Map;

public interface AuthFacade {
    BaseResponse<Boolean> login(LoginRequest request, HttpServletResponse servletResponse);

    BaseResponse<ProfileResponse> findProfile();

    BaseResponse<Boolean> refreshToken(HttpServletRequest servletRequest, HttpServletResponse servletResponse);

    BaseResponse<Boolean> signup(SignupRequest request);

    URI activateUser(String token);

    BaseResponse<Boolean> forgotPassword(ForgotPasswordRequest request);

    Response verifyForgotPassword(String token);
}
