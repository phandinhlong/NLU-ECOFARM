package jakarta.ecofarm_backend.infrastructure.interceptor;

import jakarta.annotation.Priority;
import jakarta.ecofarm_backend.common.constant.FilterOrder;
import jakarta.ecofarm_backend.common.constant.SecurityConstant;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.exception.EntityNotFoundException;
import jakarta.ecofarm_backend.common.exception.JwtInvalidException;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Role;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.security.UserSecurityDetailsPrincipal;
import jakarta.ecofarm_backend.infrastructure.service.JwtService;
import jakarta.ecofarm_backend.infrastructure.service.UserService;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.MDC;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Provider
@PreMatching
@Priority(FilterOrder.AUTHN)
public class AuthTokenInterceptor implements ContainerRequestFilter {

    @Inject private JwtService jwtService;
    @Inject private UserService userService;
    @Context private HttpServletRequest request;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auths/login", "/api/v1/auths/refresh", "/api/v1/openapi", "/api/v1/health","/api/v1/hello-world");

    private boolean isPublicEndpoint(ContainerRequestContext ctx) {
        if ("OPTIONS".equalsIgnoreCase(ctx.getMethod())) return true;
        String uri = ctx.getUriInfo().getRequestUri().getPath();
        String path = uri.substring(request.getContextPath().length());
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        if (isPublicEndpoint(ctx)) return;

        Cookie cookie = ctx.getCookies().get(SecurityConstant.ACCEPT_TOKEN_COOKIE_NAME);
        if (cookie == null) {
            throw new JwtInvalidException(ErrorCode.JWT_INVALID);
        }

        String token = cookie.getValue();

        if (!this.jwtService.validateToken(token))
            throw new JwtInvalidException(ErrorCode.JWT_INVALID);

        String email = jwtService.getEmailFromJwtToken(token);

        User user = userService.findByEmailAndFetchRoles(email)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getRoleName().toString())
                .collect(Collectors.toSet());

        UserSecurityDetailsPrincipal details = UserSecurityDetailsPrincipal.builder().id(user.getId()).email(user.getEmail()).roles(roles).build();
        SecurityContext old = ctx.getSecurityContext();

        ctx.setSecurityContext(new SecurityContext() {
            @Override public Principal getUserPrincipal() {
                return details;
            }

            @Override public boolean isUserInRole(String role) {
                return details.hasRole(role);
            }

            @Override public boolean isSecure() {
                return old.isSecure();
            }

            @Override public String getAuthenticationScheme() {
                return "Cookie";
            }
        });

        MDC.put("email", email);
    }
}