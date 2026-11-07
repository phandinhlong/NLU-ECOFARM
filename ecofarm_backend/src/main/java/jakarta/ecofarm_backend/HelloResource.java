package jakarta.ecofarm_backend;

import jakarta.ecofarm_backend.common.response.BaseResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/hello-world")
@Tag(name = "System", description = "API hệ thống và kiểm tra tình trạng")
public class HelloResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Kiểm tra tình trạng hoạt động (Health check)",
            description = "Endpoint kiểm tra kết nối tới Backend, trả về trạng thái hoạt động của hệ thống"
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Hệ thống hoạt động bình thường",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = BaseResponse.class)
                    )
            )
    })
    public BaseResponse<String> hello() {
        return BaseResponse.ok("Hello, World! NLU-ECOFARM Backend is running.");
    }
}
