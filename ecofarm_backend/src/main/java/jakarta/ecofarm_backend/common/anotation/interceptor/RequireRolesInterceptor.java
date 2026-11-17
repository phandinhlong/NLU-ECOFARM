package jakarta.ecofarm_backend.common.anotation.interceptor;

import jakarta.annotation.Priority;
import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.SecurityContext;

import java.util.Arrays;

@Interceptor
@RequireRoles({})
@Priority(Interceptor.Priority.APPLICATION)
public class RequireRolesInterceptor {

    @Inject
    private SecurityContext securityContext;

    @AroundInvoke
    public Object manage(InvocationContext context) throws Exception {
        RequireRoles requireRoles = findRequireRoles(context);

        if (requireRoles != null) {
            checkAuthenticated();
            checkRoles(requireRoles);
        }

        return context.proceed();
    }

    private RequireRoles findRequireRoles(InvocationContext context) {
        RequireRoles onMethod = context.getMethod().getAnnotation(RequireRoles.class);
        if (onMethod != null) {
            return onMethod;
        }
        return context.getMethod().getDeclaringClass().getAnnotation(RequireRoles.class);
    }

    private void checkAuthenticated() {
        if (securityContext.getUserPrincipal() == null) {
            throw new NotAuthorizedException("Cookie");   // 401
        }
    }

    private void checkRoles(RequireRoles requireRoles) {
        String[] requiredRoles = requireRoles.value();

        if (requiredRoles.length == 0) {
            return;
        }

        boolean hasPermission = requireRoles.requireAll()
                ? hasAllRoles(requiredRoles)
                : hasAnyRole(requiredRoles);

        if (!hasPermission) {
            throw new ForbiddenException();
        }
    }

    private boolean hasAllRoles(String[] requiredRoles) {
        return Arrays.stream(requiredRoles).allMatch(securityContext::isUserInRole);
    }

    private boolean hasAnyRole(String[] requiredRoles) {
        return Arrays.stream(requiredRoles).anyMatch(securityContext::isUserInRole);
    }
}