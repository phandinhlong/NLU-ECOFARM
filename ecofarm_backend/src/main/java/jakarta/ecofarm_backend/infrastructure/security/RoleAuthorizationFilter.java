package jakarta.ecofarm_backend.infrastructure.security;

import jakarta.annotation.Priority;
import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.exception.PermissionDeniedException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.lang.reflect.Method;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class RoleAuthorizationFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext ctx) {
        Method method = resourceInfo.getResourceMethod();
        RequireRoles required = method.getAnnotation(RequireRoles.class);
        if (required == null) {
            required = resourceInfo.getResourceClass().getAnnotation(RequireRoles.class);
        }
        if (required == null) {
            return;
        }

        SecurityContext securityContext = ctx.getSecurityContext();
        for (String role : required.value()) {
            if (securityContext != null && securityContext.isUserInRole(role)) {
                return;
            }
        }
        throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
    }
}
