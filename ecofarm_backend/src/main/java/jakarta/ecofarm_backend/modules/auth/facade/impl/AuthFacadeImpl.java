package jakarta.ecofarm_backend.modules.auth.facade.impl;

import jakarta.ecofarm_backend.common.constant.SecurityConstant;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.exception.EntityNotFoundException;
import jakarta.ecofarm_backend.common.exception.JwtInvalidException;
import jakarta.ecofarm_backend.common.exception.PermissionDeniedException;
import jakarta.ecofarm_backend.common.request.LoginRequest;
import jakarta.ecofarm_backend.common.response.BaseResponse;
import jakarta.ecofarm_backend.common.response.ProfileResponse;
import jakarta.ecofarm_backend.common.util.CookieBuilderUtil;
import jakarta.ecofarm_backend.common.util.CurrentUserUtil;
import jakarta.ecofarm_backend.common.util.PasswordEncoderUtil;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.security.UserSecurityDetailsPrincipal;
import jakarta.ecofarm_backend.infrastructure.service.JwtService;
import jakarta.ecofarm_backend.infrastructure.service.UserService;
import jakarta.ecofarm_backend.modules.auth.facade.AuthFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Context;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Arrays;
import java.util.Optional;

@ApplicationScoped
public class AuthFacadeImpl implements AuthFacade {

    @Inject
    private UserService userService;

    @Inject
    private JwtService jwtService;

    @Context
    private HttpServletResponse servletResponse;

    @Context
    private HttpServletRequest servletRequest;

    @Inject
    private CurrentUserUtil currentUser;

    @Inject
    private CookieBuilderUtil cookieBuilder;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.cookie.access-max-age", defaultValue = "900")
    private Integer accessMaxAge;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.cookie.refresh-max-age", defaultValue = "604800")
    private Integer refreshMaxAge;



    @Override
    public BaseResponse<Boolean> login(LoginRequest request) {

        String email = request.getEmail();

        User user = userService.findByEmailAndFetchRoles(email)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        if (!PasswordEncoderUtil.matches(request.getPassword(),user.getPassword()))
            throw new PermissionDeniedException(ErrorCode.PASSWORD_OR_EMAIL_INVALID);

        String accessToken = jwtService.generateAccessToken(email);
        String refreshToken = jwtService.generateRefreshToken(email);

        String accessTokenCookie = cookieBuilder.buildCookie(
                SecurityConstant.ACCESS_TOKEN_COOKIE_NAME, accessToken, "/", accessMaxAge);
        String refreshTokenCookie = cookieBuilder.buildCookie(
                SecurityConstant.REFRESH_TOKEN_COOKIE_NAME, refreshToken, "/", refreshMaxAge);

        servletResponse.addHeader("Set-Cookie", accessTokenCookie);
        servletResponse.addHeader("Set-Cookie", refreshTokenCookie);

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

    @Override
    public BaseResponse<Boolean> refreshToken() {
        Optional<Cookie> cookieOpt = Arrays.stream(this.servletRequest.getCookies())
                .filter(c ->c.getName().equals(SecurityConstant.REFRESH_TOKEN_COOKIE_NAME))
                .findFirst();

        if (cookieOpt.isEmpty())
            throw new JwtInvalidException(ErrorCode.JWT_INVALID);

        String refreshToken = cookieOpt.get().getValue();

        if (!this.jwtService.validateToken(refreshToken))
            throw new JwtInvalidException(ErrorCode.JWT_INVALID);

        String email = this.jwtService.getEmailFromJwtToken(refreshToken);

        if (!this.userService.hasUserByEmail(email))
            throw new EntityNotFoundException(ErrorCode.USER_NOT_FOUND);

        String accessToken = jwtService.generateAccessToken(email);
        String acceptTokenCookie = this.cookieBuilder
                .buildCookie(SecurityConstant.ACCESS_TOKEN_COOKIE_NAME,accessToken,"/",this.accessMaxAge);

        servletResponse.addHeader("Set-Cookie", acceptTokenCookie);

        return BaseResponse.build(true);
    }

    private String buildCookie(String name, String value, String path, int maxAgeSeconds) {
        return name + "=" + value
                + "; Path=" + path
                + "; Max-Age=" + maxAgeSeconds
                + "; HttpOnly; Secure; SameSite=Lax";
    }
}
