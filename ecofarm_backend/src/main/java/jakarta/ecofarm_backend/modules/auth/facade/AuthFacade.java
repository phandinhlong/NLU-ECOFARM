package jakarta.ecofarm_backend.modules.auth.facade;

import jakarta.ecofarm_backend.common.dto.request.ForgotPasswordRequest;
import jakarta.ecofarm_backend.common.dto.request.LoginRequest;
import jakarta.ecofarm_backend.common.dto.request.SignupRequest;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.ProfileResponse;
import jakarta.validation.Valid;

import java.net.URI;

public interface AuthFacade {
    BaseResponse<Boolean> login(LoginRequest request);

    BaseResponse<ProfileResponse> findProfile();

    BaseResponse<Boolean> refreshToken();

    BaseResponse<Boolean> signup(SignupRequest request);

    URI activateUser(String token);

    BaseResponse<Boolean> forgotPassword(ForgotPasswordRequest request);
}
