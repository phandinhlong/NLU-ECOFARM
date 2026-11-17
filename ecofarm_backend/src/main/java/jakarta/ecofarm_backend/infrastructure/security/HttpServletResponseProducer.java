package jakarta.ecofarm_backend.infrastructure.security;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.servlet.http.HttpServletResponse;
import org.jboss.resteasy.core.ResteasyContext;

/**
 * Cung cấp HttpServletResponse của request hiện tại cho các CDI bean,
 * để có thể dùng @Inject thay vì phải truyền tham số qua từng tầng.
 */
@RequestScoped
public class HttpServletResponseProducer {

    @Produces
    @RequestScoped
    public HttpServletResponse produceHttpServletResponse() {
        return ResteasyContext.getContextData(HttpServletResponse.class);
    }
}
