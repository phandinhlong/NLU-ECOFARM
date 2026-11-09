package jakarta.ecofarm_backend.rest;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.security.Principal;

@Provider
public class AllowAllSecurityFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return new Principal() {
                    @Override
                    public String getName() {
                        return "admin";
                    }
                };
            }

            @Override
            public boolean isUserInRole(String role) {
                return "ADMIN".equalsIgnoreCase(role) || "PARTNER_QC".equalsIgnoreCase(role) || "REGULATOR".equalsIgnoreCase(role);
            }

            @Override
            public boolean isSecure() {
                return requestContext.getUriInfo().getRequestUri().getScheme().equals("https");
            }

            @Override
            public String getAuthenticationScheme() {
                return "Basic";
            }
        });
    }
}
