package jakarta.ecofarm_backend.modules.auth.facade.impl;

import jakarta.ecofarm_backend.common.dto.request.LoginRequest;
import jakarta.ecofarm_backend.common.dto.request.SignupRequest;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.ProfileResponse;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.enums.RoleEnum;
import jakarta.ecofarm_backend.common.exception.EntityNotFoundException;
import jakarta.ecofarm_backend.common.exception.JwtInvalidException;
import jakarta.ecofarm_backend.common.exception.PermissionDeniedException;
import jakarta.ecofarm_backend.common.util.PasswordEncoderUtil;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Role;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.service.EmailService;
import jakarta.ecofarm_backend.infrastructure.service.JwtService;
import jakarta.ecofarm_backend.infrastructure.service.RoleService;
import jakarta.ecofarm_backend.infrastructure.service.UserService;
import jakarta.ecofarm_backend.infrastructure.service.impl.JwtServiceImpl;
import jakarta.ecofarm_backend.modules.auth.facade.AuthFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.util.Map;

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
    @ConfigProperty(name = "nlu.eco.farm.backend.activate.user.api.url", defaultValue = "http://localhost:8080")
    private String backendActivateUserUrl;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.frontend.successfully.url", defaultValue = "http://localhost:300")
    private String frontendActivationSuccessfullyUrl;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.frontend.failure.url", defaultValue = "http://localhost:300")
    private String frontendActivationFailureUrl;

    private static final Logger log = LogManager.getLogger(AuthFacadeImpl.class);

    @Override
    public BaseResponse<Map<String, String>> login(LoginRequest request) {
        String email = request.getEmail();

        User user = userService.findByEmailAndFetchRoles(email)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive())
            throw new EntityNotFoundException(ErrorCode.REQUIRE_ACTIVE_USER);

        if (!PasswordEncoderUtil.matches(request.getPassword(), user.getPassword()))
            throw new PermissionDeniedException(ErrorCode.PASSWORD_OR_EMAIL_INVALID);

        return BaseResponse.build(Map.of(
                "accessToken", jwtService.generateAccessToken(email),
                "refreshToken", jwtService.generateRefreshToken(email)));
    }

    @Override
    public BaseResponse<Map<String, String>> refreshTokens(String refreshToken) {
        if (refreshToken == null || !jwtService.validateToken(refreshToken)
                || !jwtService.hasTokenType(refreshToken, JwtServiceImpl.TYPE_REFRESH))
            throw new JwtInvalidException(ErrorCode.JWT_INVALID);

        String email = jwtService.getEmailFromJwtToken(refreshToken);

        if (Boolean.FALSE.equals(userService.hasUserByEmail(email)))
            throw new EntityNotFoundException(ErrorCode.USER_NOT_FOUND);

        return BaseResponse.build(Map.of("accessToken", jwtService.generateAccessToken(email)));
    }

    @Override
    public BaseResponse<ProfileResponse> findProfile(String email) {
        User user = this.userService.findByEmailAndFetchRoles(email)
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
    @Transactional
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
    @Transactional
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
}
