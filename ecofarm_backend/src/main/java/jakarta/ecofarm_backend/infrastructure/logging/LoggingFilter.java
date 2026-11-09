package jakarta.ecofarm_backend.infrastructure.logging;

import jakarta.annotation.Priority;
import jakarta.ecofarm_backend.common.constant.FilterOrder;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.io.IOException;
import java.security.Principal;
import java.util.UUID;

@Provider
@PreMatching
@Priority(FilterOrder.LOGGING)
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger log = LogManager.getLogger(LoggingFilter.class);
    private static final String START_TIME_PROP = "logging.startTime";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        long startTime = System.currentTimeMillis();
        requestContext.setProperty(START_TIME_PROP, startTime);

        String requestId = requestContext.getHeaderString("X-Request-Id");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        ThreadContext.put("requestId", requestId);

        SecurityContext securityContext = requestContext.getSecurityContext();
        if (securityContext != null) {
            Principal principal = securityContext.getUserPrincipal();
            if (principal != null) {
                ThreadContext.put("email", principal.getName());
            }
        }

        String method = requestContext.getMethod();
        UriInfo uriInfo = requestContext.getUriInfo();
        String path = uriInfo.getRequestUri().getRawPath();
        String query = uriInfo.getRequestUri().getRawQuery();
        String fullPath = (query != null && !query.isBlank()) ? path + "?" + query : path;

        String clientIp = requestContext.getHeaderString("X-Forwarded-For");
        if (clientIp == null || clientIp.isBlank()) {
            clientIp = requestContext.getHeaderString("X-Real-IP");
        }

        log.info("[Incoming] {} {} [clientIp={}]", method, fullPath, clientIp != null ? clientIp : "direct");
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        try {
            Long startTime = (Long) requestContext.getProperty(START_TIME_PROP);
            long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;

            String method = requestContext.getMethod();
            String path = requestContext.getUriInfo().getRequestUri().getRawPath();
            int status = responseContext.getStatus();
            String requestId = ThreadContext.get("requestId");

            if (requestId != null) {
                responseContext.getHeaders().putSingle("X-Request-Id", requestId);
            }

            log.info("[Outgoing] {} {} [Status: {}] (took {} ms)", method, path, status, duration);
        } finally {
            ThreadContext.clearMap();
        }
    }
}
