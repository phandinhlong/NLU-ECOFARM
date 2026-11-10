package jakarta.ecofarm_backend.infrastructure.cors;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.ecofarm_backend.common.constant.FilterOrder;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Provider
@PreMatching
@Priority(FilterOrder.CORS)
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private List<String> allowedOrigins = new ArrayList<>(List.of("*"));
    private String allowedMethods = "GET,POST,PUT,DELETE,PATCH,OPTIONS";
    private String allowedHeaders = "Content-Type,Authorization,X-Requested-With,X-CSRF-Token,accept_token,refresh_token";
    private String maxAge = "3600";
    private boolean allowAll = true;

    @PostConstruct
    void init() {
        try {
            Config config = ConfigProvider.getConfig();
            config.getOptionalValue("cors.allowed.origins", String.class).ifPresent(origins -> {
                allowedOrigins = Arrays.stream(origins.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
            });
            config.getOptionalValue("cors.allowed.methods", String.class).ifPresent(m -> allowedMethods = m);
            config.getOptionalValue("cors.allowed.headers", String.class).ifPresent(h -> allowedHeaders = h);
            config.getOptionalValue("cors.max.age", String.class).ifPresent(a -> maxAge = a);
        } catch (Throwable ignored) {
            try (java.io.InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/microprofile-config.properties")) {
                if (is != null) {
                    java.util.Properties props = new java.util.Properties();
                    props.load(is);
                    String origins = props.getProperty("cors.allowed.origins");
                    if (origins != null) {
                        allowedOrigins = Arrays.stream(origins.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList());
                    }
                    if (props.getProperty("cors.allowed.methods") != null) allowedMethods = props.getProperty("cors.allowed.methods");
                    if (props.getProperty("cors.allowed.headers") != null) allowedHeaders = props.getProperty("cors.allowed.headers");
                    if (props.getProperty("cors.max.age") != null) maxAge = props.getProperty("cors.max.age");
                }
            } catch (Throwable e) {
                // ignore
            }
        }

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