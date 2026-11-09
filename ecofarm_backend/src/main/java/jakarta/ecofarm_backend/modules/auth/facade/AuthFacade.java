package jakarta.ecofarm_backend.modules.auth.facade;

import jakarta.ecofarm_backend.common.request.LoginRequest;
import jakarta.ecofarm_backend.common.response.BaseResponse;
import jakarta.ecofarm_backend.common.response.ProfileResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthFacade {
    BaseResponse<Boolean> login(LoginRequest request);

    BaseResponse<ProfileResponse> findProfile();
}
