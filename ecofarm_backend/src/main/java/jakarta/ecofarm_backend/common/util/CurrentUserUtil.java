package jakarta.ecofarm_backend.common.util;

import jakarta.ecofarm_backend.infrastructure.security.UserSecurityDetailsPrincipal;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

@RequestScoped
public class CurrentUserUtil {
    @Context
    SecurityContext securityContext;

    public UserSecurityDetailsPrincipal get() {
        return (UserSecurityDetailsPrincipal) securityContext.getUserPrincipal();
    }
}