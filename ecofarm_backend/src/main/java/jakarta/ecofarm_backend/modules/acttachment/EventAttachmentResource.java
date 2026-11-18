package jakarta.ecofarm_backend.modules.acttachment;


import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/v1/event/attachments")
@Tag(name = "Event attachments", description = "Quản lý lô canh tác")
@RequireRoles(value = {"ADMIN", "ENGINEER", "QUALITY_CONTROL"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventAttachmentResource {

}
