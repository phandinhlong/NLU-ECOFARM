package jakarta.ecofarm_backend.modules.auth.facade.impl;

import jakarta.ecofarm_backend.common.anotation.DatabaseTransactional;
import jakarta.ecofarm_backend.common.constant.SecurityConstant;
import jakarta.ecofarm_backend.common.dto.request.ForgotPasswordRequest;
import jakarta.ecofarm_backend.common.dto.request.LoginRequest;
import jakarta.ecofarm_backend.common.dto.request.SignupRequest;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.ProfileResponse;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.enums.RoleEnum;
import jakarta.ecofarm_backend.common.exception.EntityNotFoundException;
import jakarta.ecofarm_backend.common.exception.JwtInvalidException;
import jakarta.ecofarm_backend.common.exception.PermissionDeniedException;
import jakarta.ecofarm_backend.common.util.CookieBuilderUtil;
import jakarta.ecofarm_backend.common.util.CurrentUserUtil;
import jakarta.ecofarm_backend.common.util.PasswordEncoderUtil;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Role;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.security.UserSecurityDetailsPrincipal;
import jakarta.ecofarm_backend.infrastructure.service.EmailService;
import jakarta.ecofarm_backend.infrastructure.service.JwtService;
import jakarta.ecofarm_backend.infrastructure.service.RoleService;
import jakarta.ecofarm_backend.infrastructure.service.UserService;
import jakarta.ecofarm_backend.infrastructure.service.impl.JwtServiceImpl;
import jakarta.ecofarm_backend.modules.auth.facade.AuthFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.servlet.http.Cookie;
import jakarta.ws.rs.core.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class AuthFacadeImpl implements AuthFacade {

    @Inject
    private UserService userService;

    @Inject
    private JwtService jwtService;

    @Inject
    private RoleService roleService;

    @Inject
    private EmailService emailService;

    @Inject
    private CookieBuilderUtil cookieBuilder;

    @Inject
//    @Context
    private HttpServletResponse servletResponse;

//    @Context
    @Inject
    private HttpServletRequest servletRequest;

    @Inject
    private CurrentUserUtil currentUser;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.backend.activate.user.api.url", defaultValue = "http://localhost:8080")
    private String backendActivateUserUrl;


    @Inject
    @ConfigProperty(name = "nlu.eco.farm.backend.forgot-password-verify.api.url", defaultValue = "http://localhost:8080")
    private String backendForgotPasswordVerify;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.frontend.successfully.url", defaultValue = "http://localhost:300")
    private String frontendActivationSuccessfullyUrl;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.frontend.failure.url", defaultValue = "http://localhost:300")
    private String frontendActivationFailureUrl;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.frontend.reset.password.successfully.url", defaultValue = "http://localhost:8080/ecofarm_backend-1.0-SNAPSHOT/api/v1/auths/forgot-password/verify")
    private String frontendResetPasswordSuccessfullyUrl;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.frontend.reset.password.failure.url", defaultValue = "http://localhost:300")
    private String frontendResetPasswordFailureUrl;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.cookie.access-max-age", defaultValue = "900")
    private Integer accessMaxAge;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.cookie.refresh-max-age", defaultValue = "604800")
    private Integer refreshMaxAge;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.cookie.forgot-password", defaultValue = "900")
    private Integer forgotPasswordMaxAge;

    private static final Logger log = LogManager.getLogger(AuthFacadeImpl.class);

    @Override
    public BaseResponse<Boolean> login(LoginRequest request) {

        String email = request.getEmail();

        User user = userService.findByEmailAndFetchRoles(email)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive())
            throw  new EntityNotFoundException(ErrorCode.REQUIRE_ACTIVE_USER);

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
    public BaseResponse<Boolean> refreshToken() {
        Optional<Cookie> cookieOpt = Arrays.stream(this.servletRequest.getCookies())
                .filter(c ->c.getName().equals(SecurityConstant.REFRESH_TOKEN_COOKIE_NAME))
                .findFirst();

        if (cookieOpt.isEmpty())
            throw new JwtInvalidException(ErrorCode.JWT_INVALID);

        String refreshToken = cookieOpt.get().getValue();

        if (!this.jwtService.validateToken(refreshToken))
            throw new JwtInvalidException(ErrorCode.JWT_INVALID);

        if (!this.jwtService.hasTokenType(refreshToken, JwtServiceImpl.TYPE_REFRESH))
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

    @Override
    public BaseResponse<ProfileResponse> findProfile() {
        UserSecurityDetailsPrincipal principal = this.currentUser.get();

        User user = this.userService.findByEmailAndFetchRoles(principal.getEmail())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        ProfileResponse response = ProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRoles().stream().map(Role::getRoleName).toList())
                .build();

        return BaseResponse.<ProfileResponse>build(response);
    }

    @Override
    @DatabaseTransactional
    public BaseResponse<Boolean> signup(SignupRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword()))
            throw new PermissionDeniedException(ErrorCode.PASSWORD_NOT_MATCH);

        if (this.userService.hasUserByEmail(request.getEmail()))
            throw new PermissionDeniedException(ErrorCode.EMAIL_ALREADY_EXISTS);

        Role role = this.roleService.findByRoleEnum(RoleEnum.USER)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ROLE_NOT_FOUND));

        String passwordEncoded = PasswordEncoderUtil.encode(request.getPassword());

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoded)
                .isActive(false)
                .build();

        user.addRole(role);

        this.userService.save(user);

        String activateUserUrl = this.buildActivateUrl(user.getEmail());

        log.info(activateUserUrl);

        this.emailService.sendActiveUserEmail(user.getEmail(), activateUserUrl);

        return BaseResponse.build(true);
    }

    private String buildActivateUrl(String email) {
        String activateAccountToken = this.jwtService.generateAccountActivationToken(email);
        return String.format("%s?token=%s", this.backendActivateUserUrl, activateAccountToken);
    }

    @Override
    @DatabaseTransactional
    public URI activateUser(String token) {
        if (token == null || (!this.jwtService.validateToken(token))
                || (!this.jwtService.hasTokenType(token, JwtServiceImpl.TYPE_ACCOUNT_ACTIVATION_TOKEN)))
            return UriBuilder.fromUri(this.frontendActivationFailureUrl)
                    .queryParam("status", "inactive-user")
                    .build();

        String email = this.jwtService.getEmailFromJwtToken(token);

        User user = this.userService.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        user.activeUser();

        this.userService.save(user);

        return UriBuilder.fromUri(this.frontendActivationSuccessfullyUrl)
                .queryParam("status", "active-user")
                .build();
    }

    @Override
    public BaseResponse<Boolean> forgotPassword(ForgotPasswordRequest request) {
        if (this.userService.hasUserByEmail(request.getEmail()))
            throw new PermissionDeniedException(ErrorCode.EMAIL_ALREADY_EXISTS);

        String forgotPasswordUrl = this.buildForgotPasswordUrl(request.getEmail());

        this.emailService.sendForgotPasswordEmail(request.getEmail(), forgotPasswordUrl);

        return BaseResponse.build(true);
    }

    @Override
    public Response verifyForgotPassword(String token) {
        if (token == null || (!this.jwtService.validateToken(token))
                || (!this.jwtService.hasTokenType(token, JwtServiceImpl.TYPE_FORGOT_PASSWORD_TOKEN))){

           URI failure = UriBuilder.fromUri(this.frontendResetPasswordFailureUrl)
                    .queryParam("status", "invalid-token")
                    .build();
           return  Response.seeOther(failure).build();
        }

        String email = this.jwtService.getEmailFromJwtToken(token);

        if (this.userService.hasUserByEmail(email))
            throw new PermissionDeniedException(ErrorCode.EMAIL_ALREADY_EXISTS);

        String resetToken = this.jwtService.generateForgotPasswordToken(email);

        String resetPasswordCookie = this.cookieBuilder.buildCookie(
                SecurityConstant.ACCESS_TOKEN_COOKIE_NAME,
                resetToken,
                "/",
                forgotPasswordMaxAge
                );

        URI success = UriBuilder.fromUri(this.frontendResetPasswordSuccessfullyUrl)
                .queryParam("status", "reset-password")
                .build();

        Response.ResponseBuilder builder = Response.seeOther(success);

        builder.header(HttpHeaders.SET_COOKIE, resetPasswordCookie);

        return builder.build();
    }


    private String buildForgotPasswordUrl(String email) {
        String activateAccountToken = this.jwtService.generateForgotPasswordToken(email);
        return String.format("%s?token=%s&email=%s", this.backendForgotPasswordVerify, activateAccountToken,email);
    }
}
