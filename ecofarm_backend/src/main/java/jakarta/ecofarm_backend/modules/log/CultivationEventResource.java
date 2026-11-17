package jakarta.ecofarm_backend.modules.log;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.response.AttachmentResponse;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.EventResponse;
import jakarta.ecofarm_backend.common.dto.response.PageResponse;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.exception.PermissionDeniedException;
import jakarta.ecofarm_backend.infrastructure.security.UserSecurityDetailsPrincipal;
import jakarta.ecofarm_backend.modules.log.EventRequests.CreateEventRequest;
import jakarta.ecofarm_backend.modules.log.facade.LogFacade;
import jakarta.ecofarm_backend.modules.log.facade.LogFacade.UploadInput;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Các endpoint nhật ký canh tác cho một lô. Resource này chỉ chuyển đổi HTTP sang {@link LogFacade}:
 * lấy actor và vai trò từ security context, đọc dữ liệu tệp từ request, rồi map kết quả của facade
 * thành response. Toàn bộ logic nghiệp vụ, khóa dòng, transaction và lưu trữ nằm trong facade.
 */
@Path("/v1/batches/{batchId}/events")
@Produces(MediaType.APPLICATION_JSON)
@RequireRoles({"ADMIN", "ENGINEER", "WORKER", "FARMER", "PACKER", "REGULATOR"})
public class CultivationEventResource {

    @Inject
    private LogFacade logFacade;

    @Context
    private SecurityContext securityContext;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.log.clock-skew.threshold-ms", defaultValue = "300000")
    private long clockSkewThresholdMs;

    @POST
    @Path("/attachments")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(@PathParam("batchId") Long batchId, List<EntityPart> parts) throws IOException {
        List<UploadInput> files = new ArrayList<>();
        if (parts != null) {
            for (EntityPart part : parts) {
                String mime = part.getMediaType() == null ? null : part.getMediaType().toString();
                try (InputStream in = part.getContent()) {
                    files.add(new UploadInput(mime, in.readAllBytes()));
                }
            }
        }
        BaseResponse<List<AttachmentResponse>> result =
                logFacade.upload(batchId, files, requireActorId(), engineerOrAdmin());
        return Response.status(Response.Status.CREATED).entity(result).build();
    }

    @GET
    @Path("/attachments/{fileKey}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response download(@PathParam("batchId") Long batchId, @PathParam("fileKey") String fileKey) throws IOException {
        LogFacade.DownloadResult result = logFacade.download(batchId, fileKey, requireActorId(), engineerOrAdmin());
        return Response.ok(result.bytes()).type(result.mimeType()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@PathParam("batchId") Long batchId, CreateEventRequest request) {
        BaseResponse<EventResponse> result =
                logFacade.create(batchId, request, requireActorId(), engineerOrAdmin(), clockSkewThresholdMs);
        return Response.status(Response.Status.CREATED).entity(result).build();
    }

    @GET
    public BaseResponse<PageResponse<EventResponse>> list(@PathParam("batchId") Long batchId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("status") String status,
            @QueryParam("eventType") String eventType,
            @QueryParam("includeSuperseded") @DefaultValue("true") boolean includeSuperseded) {
        Long actorId = securityContext.getUserPrincipal() instanceof UserSecurityDetailsPrincipal u ? u.getId() : null;
        boolean workerOrFarmer = securityContext.isUserInRole("WORKER") || securityContext.isUserInRole("FARMER");
        return logFacade.list(batchId, page, size, status, eventType, includeSuperseded,
                actorId, engineerOrAdmin(), workerOrFarmer);
    }

    private Long requireActorId() {
        if (!(securityContext.getUserPrincipal() instanceof UserSecurityDetailsPrincipal user)) {
            throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
        }
        return user.getId();
    }

    private boolean engineerOrAdmin() {
        return securityContext.isUserInRole("ADMIN") || securityContext.isUserInRole("ENGINEER");
    }
}
