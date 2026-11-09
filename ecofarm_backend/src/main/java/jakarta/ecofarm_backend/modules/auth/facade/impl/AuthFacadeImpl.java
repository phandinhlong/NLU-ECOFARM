package jakarta.ecofarm_backend.modules.auth.facade.impl;

import jakarta.ecofarm_backend.common.constant.SecurityConstant;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.exception.EntityNotFoundException;
import jakarta.ecofarm_backend.common.exception.PermissionDeniedException;
import jakarta.ecofarm_backend.common.request.LoginRequest;
import jakarta.ecofarm_backend.common.response.BaseResponse;
import jakarta.ecofarm_backend.common.response.ProfileResponse;
import jakarta.ecofarm_backend.common.util.CurrentUserUtil;
import jakarta.ecofarm_backend.common.util.PasswordEncoderUtil;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.security.UserSecurityDetailsPrincipal;
import jakarta.ecofarm_backend.infrastructure.service.JwtService;
import jakarta.ecofarm_backend.infrastructure.service.UserService;
import jakarta.ecofarm_backend.modules.auth.facade.AuthFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Context;

@ApplicationScoped
public class AuthFacadeImpl implements AuthFacade {

    @Inject
    private UserService userService;

    @Inject
    private JwtService jwtService;

    @Context
    private HttpServletResponse servletResponse;

    @Inject
    private CurrentUserUtil currentUser;



    @Override
    public BaseResponse<Boolean> login( LoginRequest request) {

        String email = request.getEmail();

        User user = userService.findByEmailAndFetchRoles(email)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        if (!PasswordEncoderUtil.matches(request.getPassword(),user.getPassword()))
            throw new PermissionDeniedException(ErrorCode.PASSWORD_OR_EMAIL_INVALID);

        String accessToken = jwtService.generateAccessToken(email);
        String refreshToken = jwtService.generateRefreshToken(email);

        servletResponse.addHeader("Set-Cookie", buildCookie(SecurityConstant.ACCEPT_TOKEN_COOKIE_NAME, accessToken, "/", 15 * 60));
        servletResponse.addHeader("Set-Cookie", buildCookie(SecurityConstant.REFRESH_TOKEN_COOKIE_NAME, refreshToken, "/", 7 * 24 * 60 * 60));

        return BaseResponse.build(true);
    }

    @Override
    public BaseResponse<ProfileResponse> findProfile() {
        UserSecurityDetailsPrincipal principal = this.currentUser.get();

        User user = this.userService.findById(principal.getId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        ProfileResponse response = ProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();

       return BaseResponse.<ProfileResponse>build(response);
    }

    private String buildCookie(String name, String value, String path, int maxAgeSeconds) {
        return name + "=" + value
                + "; Path=" + path
                + "; Max-Age=" + maxAgeSeconds
                + "; HttpOnly; Secure; SameSite=Lax";
    }
}
