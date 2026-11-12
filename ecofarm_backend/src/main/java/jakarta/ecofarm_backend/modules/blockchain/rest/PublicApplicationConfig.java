package jakarta.ecofarm_backend.modules.blockchain.rest;

import jakarta.ecofarm_backend.infrastructure.cors.CorsFilter;
import jakarta.ecofarm_backend.infrastructure.handle_exception.EntityNotFoundExceptionMapper;
import jakarta.ecofarm_backend.infrastructure.handle_exception.JsonParseExceptionMapper;
import jakarta.ecofarm_backend.infrastructure.handle_exception.JwtInvalidExceptionMapper;
import jakarta.ecofarm_backend.infrastructure.handle_exception.PermissionDeniedExceptionMapper;
import jakarta.ecofarm_backend.infrastructure.handle_exception.ValidationExceptionMapper;
import jakarta.ecofarm_backend.infrastructure.interceptor.PublicRateLimitFilter;
import jakarta.ecofarm_backend.infrastructure.logging.LoggingFilter;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.util.Set;

// Chi dang ky resource cong khai. Neu de Application rong, JAX-RS se dang ky
// toan bo resource (ke ca endpoint can dang nhap) vao /public va bi lo auth.
@ApplicationPath("/public")
public class PublicApplicationConfig extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
                PublicTraceResource.class,
                PublicRateLimitFilter.class,
                CorsFilter.class,
                LoggingFilter.class,
                EntityNotFoundExceptionMapper.class,
                ValidationExceptionMapper.class,
                JsonParseExceptionMapper.class,
                JwtInvalidExceptionMapper.class,
                PermissionDeniedExceptionMapper.class
        );
    }
}
