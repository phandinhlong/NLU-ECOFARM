package jakarta.ecofarm_backend.infrastructure.interceptor;

import jakarta.annotation.Priority;
import jakarta.ecofarm_backend.common.constant.SecurityConstant;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthTokenInterceptor implements ContainerRequestFilter {



    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        Cookie accessTokenCookie = requestContext.getCookies().get(SecurityConstant.ACCEPT_TOKEN_COOKIE_NAME);


//        if (c == null) {
//            requestContext.abortWith(Response.status(401).build());
//            return;
//        }
//        try {
//            UserPrincipal user = tokenService.verify(c.getValue());
//            SecurityContext old = ctx.getSecurityContext();
//            ctx.setSecurityContext(new SecurityContext() {
//                public Principal getUserPrincipal() { return user; }
//                public boolean isUserInRole(String r) { return user.getRoles().contains(r); }
//                public boolean isSecure() { return old.isSecure(); }
//                public String getAuthenticationScheme() { return "Cookie"; }
//            });
//        } catch (Exception e) {
//            ctx.abortWith(Response.status(401).build());
//        }
    }


}
