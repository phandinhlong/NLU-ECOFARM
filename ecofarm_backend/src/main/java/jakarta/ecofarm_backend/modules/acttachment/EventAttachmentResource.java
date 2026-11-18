package jakarta.ecofarm_backend.modules.acttachment;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.modules.acttachment.dto.CreateEventAttachmentRequest;
import jakarta.ecofarm_backend.modules.acttachment.facade.EventAttachmentFacade;
import jakarta.inject.Inject;
import jakarta.servlet.http.Part;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/v1/event-attachments")
@Tag(name = "Event attachments", description = "Cultivation event attachment management")
@RequireRoles(value = {"ADMIN", "ENGINEER", "QUALITY_CONTROL"})
@Produces(MediaType.APPLICATION_JSON)
public class EventAttachmentResource {

    @Inject
    private EventAttachmentFacade eventAttachmentFacade;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Create attachment for cultivation event")
    public BaseResponse<Boolean> createAttachment(
            @Valid @BeanParam CreateEventAttachmentRequest request,
            @FormParam("file") Part file) {

        return eventAttachmentFacade.createAttachment(request, file);
    }
}
