package jakarta.ecofarm_backend.infrastructure.cors;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.stream.Collectors;

@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION - 200)
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Inject
    @ConfigProperty(name = "cors.allowed.origins", defaultValue = "*")
    private List<String> allowedOrigins;

    @Inject
    @ConfigProperty(name = "cors.allowed.methods",
            defaultValue = "GET,POST,PUT,DELETE,PATCH,OPTIONS")
    private String allowedMethods;

    @Inject
    @ConfigProperty(name = "cors.allowed.headers",
            defaultValue = "Content-Type,X-Requested-With,X-CSRF-Token")
    private String allowedHeaders;

    @Inject
    @ConfigProperty(name = "cors.max.age", defaultValue = "3600")
    private String maxAge;

    private boolean allowAll;

    @PostConstruct
    void init() {
        allowedOrigins = allowedOrigins.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        allowAll = allowedOrigins.contains("*");
    }

    @Override
    public void filter(ContainerRequestContext req) {
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())
                && req.getHeaderString("Access-Control-Request-Method") != null) {
            req.abortWith(Response.ok().build());
        }
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) {
        String origin = req.getHeaderString("Origin");
        if (origin == null) return;

        MultivaluedMap<String, Object> h = res.getHeaders();

        if (allowAll) {
            h.putSingle("Access-Control-Allow-Origin", "*");
        } else if (allowedOrigins.contains(origin)) {
            h.putSingle("Access-Control-Allow-Origin", origin);
            h.putSingle("Access-Control-Allow-Credentials", "true");
            h.putSingle("Vary", "Origin");
        } else {
            return;
        }

        h.putSingle("Access-Control-Allow-Methods", allowedMethods);
        h.putSingle("Access-Control-Allow-Headers", allowedHeaders);
        h.putSingle("Access-Control-Max-Age", maxAge);
    }
}