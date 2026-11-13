package jakarta.ecofarm_backend.common.util;

import jakarta.ecofarm_backend.infrastructure.security.UserSecurityDetailsPrincipal;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

@RequestScoped
public class CurrentUserUtil {
    @Inject
    SecurityContext securityContext;

    public UserSecurityDetailsPrincipal get() {
        return (UserSecurityDetailsPrincipal) securityContext.getUserPrincipal();
    }
}