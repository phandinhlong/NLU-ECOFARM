package jakarta.ecofarm_backend.modules.ver;

import jakarta.ecofarm_backend.infrastructure.cors.CorsFilter;
import jakarta.ecofarm_backend.modules.ver.filter.RateLimitFilter;
import jakarta.ecofarm_backend.modules.ver.rest.PublicTraceResource;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.util.Set;

/**
 * JAX-RS app riêng cho trang tra cứu công khai (FR-VER-005: không cần đăng nhập).
 * Chỉ đăng ký các class trong app này nên không bị AuthTokenInterceptor chặn.
 */
@ApplicationPath("/public")
public class VerPublicApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
                PublicTraceResource.class,
                RateLimitFilter.class,
                CorsFilter.class);
    }
}
