package jakarta.ecofarm_backend.config;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@OpenAPIDefinition(
        info = @Info(
                title = "NLU-ECOFARM Backend API",
                version = "1.0.0",
                description = "Tài liệu API hệ thống Nông trại Thông minh NLU-ECOFARM",
                contact = @Contact(
                        name = "NLU-ECOFARM Team",
                        email = "ecofarm@nlu.edu.vn",
                        url = "https://ecofarm.nlu.edu.vn"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        servers = {
                @Server(url = "/ecofarm_backend_war/api", description = "Local Tomcat Server (với Context Path)"),
                @Server(url = "/api", description = "Root API Server (Payara / WildFly / Production)")
        },
        tags = {
                @Tag(name = "System", description = "API hệ thống và kiểm tra tình trạng"),
                @Tag(name = "Authentication", description = "Xác thực và phân quyền (JWT Token, Login, Register)"),
                @Tag(name = "Users", description = "Quản lý người dùng"),
                @Tag(name = "Farms", description = "Quản lý trang trại và khu vực canh tác")
        },
        security = {
                @SecurityRequirement(name = "BearerAuth")
        }
)
@SecuritySchemes(value = {
        @SecurityScheme(
                securitySchemeName = "BearerAuth",
                type = SecuritySchemeType.HTTP,
                scheme = "bearer",
                bearerFormat = "JWT",
                description = "Nhập JWT token theo định dạng: Bearer {token}"
        ),
        @SecurityScheme(
                securitySchemeName = "CookieAuth",
                type = SecuritySchemeType.APIKEY,
                in = SecuritySchemeIn.COOKIE,
                apiKeyName = "accept_token",
                description = "Xác thực qua Cookie accept_token"
        )
})
public class OpenApiConfig {
}
