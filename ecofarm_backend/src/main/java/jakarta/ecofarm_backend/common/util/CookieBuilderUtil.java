package jakarta.ecofarm_backend.common.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class CookieBuilderUtil {
    @Inject
    @ConfigProperty(name = "nlu.eco.farm.cookie.same-site", defaultValue = "Lax")
    private String sameSite;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.cookie.secure", defaultValue = "true")
    private Boolean secure;

    public String buildCookie(String name, String value, String path, int maxAgeSeconds) {
        StringBuilder sb = new StringBuilder()
                .append(name).append("=").append(value)
                .append("; Path=").append(path)
                .append("; Max-Age=").append(maxAgeSeconds)
                .append("; HttpOnly");

        if (secure || "None".equalsIgnoreCase(sameSite)) {
            sb.append("; Secure");
        }
        sb.append("; SameSite=").append(sameSite);
        return sb.toString();
    }
}
