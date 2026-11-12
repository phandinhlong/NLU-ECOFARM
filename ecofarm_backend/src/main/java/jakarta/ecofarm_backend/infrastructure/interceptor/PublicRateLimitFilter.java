package jakarta.ecofarm_backend.infrastructure.interceptor;

import jakarta.annotation.Priority;
import jakarta.ecofarm_backend.common.constant.FilterOrder;
import jakarta.ecofarm_backend.common.dto.response.ExceptionResponse;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Provider
@PreMatching
@Priority(FilterOrder.AUTHN - 100)
public class PublicRateLimitFilter implements ContainerRequestFilter {

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.public.rate-limit.max-requests", defaultValue = "30")
    private int maxRequests;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.public.rate-limit.window-seconds", defaultValue = "60")
    private long windowSeconds;

    @Context
    private HttpServletRequest servletRequest;

    private final Map<String, Deque<Long>> hitsPerIp = new ConcurrentHashMap<>();

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        String path = ctx.getUriInfo().getRequestUri().getPath();
        String contextPath = servletRequest.getContextPath();
        String appPath = path.substring(contextPath.length());
        if (!appPath.startsWith("/public/")) {
            return;
        }

        String ip = resolveClientIp(ctx);
        Deque<Long> deque = hitsPerIp.computeIfAbsent(ip, k -> new ArrayDeque<>());
        long now = System.currentTimeMillis();
        long windowMillis = windowSeconds * 1000L;

        synchronized (deque) {
            while (!deque.isEmpty() && deque.peekFirst() != null && now - deque.peekFirst() > windowMillis) {
                deque.pollFirst();
            }
            if (deque.size() >= maxRequests) {
                ctx.abortWith(buildTooManyRequests());
                return;
            }
            deque.addLast(now);
        }
    }

    private String resolveClientIp(ContainerRequestContext ctx) {
        String forwarded = ctx.getHeaderString("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        return servletRequest.getRemoteAddr();
    }

    private Response buildTooManyRequests() {
        ExceptionResponse body = ExceptionResponse.builder()
                .code(429)
                .isSuccess(false)
                .message("Quá nhiều yêu cầu tra cứu. Vui lòng thử lại sau " + windowSeconds + " giây.")
                .build();
        return Response.status(Response.Status.TOO_MANY_REQUESTS)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
