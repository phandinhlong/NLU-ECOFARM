package jakarta.ecofarm_backend.infrastructure.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.util.AuditRequestContext;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.AuditLog;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.BaseEntity;
import jakarta.ecofarm_backend.infrastructure.security.UserSecurityDetailsPrincipal;
import jakarta.ecofarm_backend.infrastructure.service.AuditLogService;
import jakarta.ecofarm_backend.infrastructure.service.UserService;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Provider
public class AuditResponseFilter implements ContainerResponseFilter {

    private static final Logger LOG = LogManager.getLogger(AuditResponseFilter.class);
    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final ObjectMapper JSON = new ObjectMapper();

    @Inject private AuditLogService auditLogService;
    @Inject private UserService userService;
    @Inject private AuditRequestContext auditContext;
    @Context private HttpServletRequest servletRequest;
    @Context private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        try {
            record(request, response);
        } catch (RuntimeException | IOException e) {
            LOG.warn("Không ghi được audit log cho {} {}", request.getMethod(), safePath(request), e);
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void record(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        String method = request.getMethod();
        String path   = request.getUriInfo().getPath().replaceFirst("^/", "");

        if (shouldSkip(method, path, response)) return;

        Long   actorId  = resolveActorId(request);
        String action   = resolveAction(method, path, request);
        String entity   = resolveEntity(path);
        Long   entityId = resolveEntityId(path, response.getEntity());
        boolean success = isSuccess(response);

        auditLogService.persist(AuditLog.builder()
                .actor(actorId == null ? null : userService.findById(actorId).orElse(null))
                .action(action)
                .entity(entity)
                .entityId(entityId == null ? 0L : entityId)
                .ipAddr(servletRequest.getRemoteAddr())
                .detail(JSON.writeValueAsString(Map.of(
                        "method", method,
                        "path",   path,
                        "status", response.getStatus(),
                        "success", success)))
                .build());
    }

    /** Returns true when the request should NOT produce an audit entry. */
    private boolean shouldSkip(String method, String path, ContainerResponseContext response) {
        if ("OPTIONS".equals(method) || "HEAD".equals(method)) return true;

        boolean login   = "v1/auths/login".equals(path);
        boolean success = isSuccess(response);

        // Non-login: only log successful write operations
        if (!login && (!success || !isWrite(method, path))) return true;

        // Activation without a resolved entity has nothing useful to record
        if ("v1/auths/activation".equals(path) && auditContext.getEntityId() == null) return true;

        return false;
    }

    private boolean isSuccess(ContainerResponseContext response) {
        if (response.getStatus() >= 400) return false;
        if (response.getEntity() instanceof BaseResponse<?> base && Boolean.FALSE.equals(base.getIsSuccess())) {
            return false;
        }
        return true;
    }

    /** Resolves the acting user's ID from the audit context or the security principal. */
    private Long resolveActorId(ContainerRequestContext request) {
        Long actorId = auditContext.getActorId();
        if (actorId == null
                && request.getSecurityContext() != null
                && request.getSecurityContext().getUserPrincipal() instanceof UserSecurityDetailsPrincipal principal) {
            actorId = principal.getId();
        }
        return actorId;
    }

    /** Derives the action name from the JAX-RS method name or falls back to the HTTP verb. */
    private String resolveAction(String httpMethod, String path, ContainerRequestContext request) {
        boolean login = "v1/auths/login".equals(path);
        if (login) {
            return isSuccess(request) ? "LOGIN" : "LOGIN_FAILED";
        }
        Method jaxrsMethod = resourceInfo == null ? null : resourceInfo.getResourceMethod();
        if (jaxrsMethod != null) {
            // e.g. createProduct → CREATE_PRODUCT
            return jaxrsMethod.getName()
                    .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                    .toUpperCase(Locale.ROOT);
        }
        return httpMethod;
    }

    /** Extracts the entity type from the URL path. */
    private String resolveEntity(String path) {
        String[] segments = path.split("/");
        String entity = segments.length > 1 ? segments[1] : segments[0];

        for (int i = 1; i < segments.length; i++) {
            if (segments[i].matches("[0-9]{1,18}")) {
                entity = segments[i - 1];
                break;
            }
        }

        if (path.startsWith("v1/auths/")) entity = "users";
        return entity;
    }

    /** Extracts the entity ID from the URL, the response body, or the audit context. */
    private Long resolveEntityId(String path, Object responseBody) {
        String[] segments = path.split("/");
        Long entityId = auditContext.getEntityId();

        for (int i = 1; i < segments.length; i++) {
            if (segments[i].matches("[0-9]{1,18}")) {
                entityId = Long.valueOf(segments[i]);
                break;
            }
        }

        if (path.startsWith("v1/auths/") && entityId == null) {
            entityId = auditContext.getActorId();
        }

        if (entityId == null) entityId = responseId(responseBody);
        return entityId;
    }

    private boolean isSuccess(ContainerRequestContext request) {
        // Used only in resolveAction for login; real success comes from the response
        return true;
    }

    // ── package-private for unit tests ───────────────────────────────────────

    static boolean isWrite(String method, String path) {
        return WRITE_METHODS.contains(method)
                || "GET".equals(method) && (
                        "v1/auths/activation".equals(path)
                        || path.matches("v1/batches/[0-9]+/qr")
                        || path.startsWith("v1/block-chain/public/trace/")
                        || path.startsWith("trace/"));
    }

    static Long responseId(Object response) {
        Object data = response instanceof BaseResponse<?> base ? base.getData() : response;
        if (data == null) return null;
        if (data instanceof BaseEntity entity) return entity.getId();
        if (!data.getClass().getPackageName().startsWith("jakarta.ecofarm_backend.")) return null;
        try {
            Object id = data.getClass().getMethod("getId").invoke(data);
            return id instanceof Number number ? number.longValue() : null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static String safePath(ContainerRequestContext request) {
        try {
            return request.getUriInfo() == null ? "?" : request.getUriInfo().getPath();
        } catch (RuntimeException e) {
            return "?";
        }
    }
}

