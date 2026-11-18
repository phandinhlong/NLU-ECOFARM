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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Provider
public class AuditResponseFilter implements ContainerResponseFilter {
    private static final Logger LOG = LogManager.getLogger(AuditResponseFilter.class);
    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final ObjectMapper JSON = new ObjectMapper();

    @Inject
    private AuditLogService auditLogService;
    @Inject
    private UserService userService;
    @Inject
    private AuditRequestContext auditContext;
    @Context
    private HttpServletRequest servletRequest;
    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        try {
            record(request, response);
        } catch (RuntimeException | IOException e) {
            LOG.warn("Không ghi được audit log cho {} {}", request.getMethod(), safePath(request), e);
        }
    }

    private static String safePath(ContainerRequestContext request) {
        try {
            return request.getUriInfo() == null ? "?" : request.getUriInfo().getPath();
        } catch (RuntimeException e) {
            return "?";
        }
    }

    private void record(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        String path = request.getUriInfo().getPath().replaceFirst("^/", "");
        boolean login = path.equals("v1/auths/login");
        boolean success = response.getStatus() < 400;
        if (response.getEntity() instanceof BaseResponse<?> base && Boolean.FALSE.equals(base.getIsSuccess())) {
            success = false;
        }
        if (!login && (!success || !isWrite(request.getMethod(), path))) {
            return;
        }
        if ("OPTIONS".equals(request.getMethod()) || "HEAD".equals(request.getMethod())) {
            return;
        }
        if (path.equals("v1/auths/activation") && auditContext.getEntityId() == null) {
            return;
        }

        Long actorId = auditContext.getActorId();
        if (actorId == null && request.getSecurityContext() != null
                && request.getSecurityContext().getUserPrincipal() instanceof UserSecurityDetailsPrincipal principal) {
            actorId = principal.getId();
        }
        Method method = resourceInfo == null ? null : resourceInfo.getResourceMethod();
        String action = login ? (success ? "LOGIN" : "LOGIN_FAILED")
                : method == null ? request.getMethod()
                : method.getName().replaceAll("([a-z0-9])([A-Z])", "$1_$2").toUpperCase(Locale.ROOT);
        String[] segments = path.split("/");
        String entity = segments.length > 1 ? segments[1] : segments[0];
        Long entityId = auditContext.getEntityId();
        for (int i = 1; i < segments.length; i++) {
            if (segments[i].matches("[0-9]{1,18}")) {
                entityId = Long.valueOf(segments[i]);
                entity = segments[i - 1];
                break;
            }
        }
        if (path.startsWith("v1/auths/")) {
            entity = "users";
            if (entityId == null) entityId = actorId;
        }
        if (entityId == null) entityId = responseId(response.getEntity());

        auditLogService.persist(AuditLog.builder()
                .actor(actorId == null ? null : userService.findById(actorId).orElse(null))
                .action(action)
                .entity(entity)
                .entityId(entityId == null ? 0L : entityId)
                .ipAddr(servletRequest.getRemoteAddr())
                .detail(JSON.writeValueAsString(Map.of("method", request.getMethod(), "path", path,
                        "status", response.getStatus(), "success", success)))
                .build());
        }

    static boolean isWrite(String method, String path) {
        return WRITE_METHODS.contains(method) || "GET".equals(method) && (
                path.equals("v1/auths/activation")
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
}
