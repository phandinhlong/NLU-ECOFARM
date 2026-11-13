package jakarta.ecofarm_backend.modules.ver.filter;

import jakarta.annotation.Priority;
import jakarta.ecofarm_backend.common.constant.FilterOrder;
import jakarta.ecofarm_backend.modules.ver.dto.ErrorResponse;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// Khong danh dau @Provider de chi duoc dang ky trong VerPublicApplication,
// tranh bi ApplicationConfig quet tu dong ap dung cho toan bo /api
@PreMatching
@Priority(FilterOrder.CORS)
public class RateLimitFilter implements ContainerRequestFilter {

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.ver.rate-limit.max-requests", defaultValue = "60")
    private Integer maxRequests;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.ver.rate-limit.window-seconds", defaultValue = "60")
    private Integer windowSeconds;

    @Context
    private HttpServletRequest request;

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    private static final class Window {
        private final long windowStart;
        private final AtomicInteger count = new AtomicInteger(0);

        Window(long windowStart) {
            this.windowStart = windowStart;
        }
    }

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(ctx.getMethod())) {
            return;
        }
        String ip = resolveClientIp();
        if (ip == null || ip.isBlank()) {
            ip = "unknown";
        }

        long now = System.currentTimeMillis();
        long windowMs = windowSeconds * 1000L;
        Window window = windows.compute(ip, (key, old) ->
                (old == null || now - old.windowStart >= windowMs) ? new Window(now) : old);

        if (window.count.incrementAndGet() > maxRequests) {
            long retryAfter = Math.max(1, (window.windowStart + windowMs - now) / 1000L);
            ctx.abortWith(Response.status(Response.Status.TOO_MANY_REQUESTS)
                    .type(MediaType.APPLICATION_JSON)
                    .header("Retry-After", String.valueOf(retryAfter))
                    .entity(ErrorResponse.builder()
                            .code("RATE_LIMITED")
                            .message("Quá nhiều yêu cầu từ địa chỉ này, vui lòng thử lại sau")
                            .isSuccess(false)
                            .build())
                    .build());
            return;
        }

        if (windows.size() > 1000) {
            windows.entrySet().removeIf(e -> now - e.getValue().windowStart >= windowMs);
        }
    }

    private String resolveClientIp() {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
