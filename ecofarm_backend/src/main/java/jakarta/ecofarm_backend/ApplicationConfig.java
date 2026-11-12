package jakarta.ecofarm_backend;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes;

@ApplicationPath("/api")
@OpenAPIDefinition(
        info = @Info(
                title = "NLU-ECOFARM Backend API",
                version = "1.0.0",
                description = "Tài liệu API của EcoFarm",
                contact = @Contact(name = "NLU-ECOFARM Team", email = "ecofarm@nlu.edu.vn")),
        security = @SecurityRequirement(name = "BearerAuth"))
@SecuritySchemes({
        @SecurityScheme(securitySchemeName = "BearerAuth",
                type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT"),
        @SecurityScheme(securitySchemeName = "CookieAuth",
                type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.COOKIE,
                apiKeyName = "accept_token")
})
public class ApplicationConfig extends Application {}
