package jakarta.ecofarm_backend.modules.packaging;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.request.ActivateBatchRequest;
import jakarta.ecofarm_backend.common.dto.request.ExtraQrRequest;
import jakarta.ecofarm_backend.common.dto.response.ActivateResponse;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.ExtraQrResponse;
import jakarta.ecofarm_backend.infrastructure.security.UserSecurityDetailsPrincipal;
import jakarta.ecofarm_backend.modules.packaging.facade.PackagingFacade;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.security.Principal;

@Path("/v1/batches")
@Tag(name = "Packaging", description = "Kích hoạt lô, sinh mã QR và xuất tem in")
@RequireRoles(value = {"ADMIN", "PACKER", "FARMER", "QUALITY_CONTROL"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PackagingResource {

    @Inject
    private PackagingFacade packagingFacade;

    @Context
    private SecurityContext securityContext;

    @Context
    private HttpServletRequest httpRequest;

    @POST
    @Path("/{batchId}/activate")
    @Operation(
            summary = "Kích hoạt lô và bắt đầu neo blockchain",
            description = "Kiểm tra điều kiện (FR-PCK-001), tạo package, dựng Merkle tree và lưu snapshot, chuyển ANCHORING, tạo outbox + anchor trong cùng một transaction."
    )
    public Response activateBatch(
            @PathParam("batchId") Long batchId,
            @Valid ActivateBatchRequest request) {
        BaseResponse<ActivateResponse> result = packagingFacade.activateBatch(
                batchId, request, resolveActorId(), resolveActorName(), resolveClientIp());
        return Response.status(Response.Status.ACCEPTED).entity(result).build();
    }

    @GET
    @Path("/{batchId}/qr")
    @Produces({"application/pdf", "text/plain", "text/csv", MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Sinh mã QR và xuất tệp in tem (PDF / ZPL / CSV)",
            description = "Chỉ lô ACTIVE mới được in. Lần đầu sinh QR cho từng đơn vị; reprint=true giữ nguyên mã cũ, đánh dấu reprint và ghi audit."
    )
    public Response exportQrLabels(
            @PathParam("batchId") Long batchId,
            @QueryParam("reprint") @DefaultValue("false") boolean reprint,
            @QueryParam("fromSerial") Integer fromSerial,
            @QueryParam("toSerial") Integer toSerial,
            @QueryParam("format") @DefaultValue("pdf") String format) {
        byte[] content = packagingFacade.exportQrLabels(
                batchId, reprint, fromSerial, toSerial, format,
                resolveActorId(), resolveActorName(), resolveClientIp());

        String normalized = format == null ? "pdf" : format.trim().toLowerCase();
        String mediaType;
        String ext;
        switch (normalized) {
            case "zpl" -> {
                mediaType = "text/plain; charset=UTF-8";
                ext = "zpl";
            }
            case "csv" -> {
                mediaType = "text/csv; charset=UTF-8";
                ext = "csv";
            }
            default -> {
                mediaType = "application/pdf";
                ext = "pdf";
            }
        }

        return Response.ok(content, mediaType)
                .header("Content-Disposition", "attachment; filename=\"batch-" + batchId + "-qr." + ext + "\"")
                .build();
    }

    @POST
    @Path("/{batchId}/qr/extra")
    @Operation(
            summary = "Sinh thêm tem QR vượt số đơn vị khai báo",
            description = "Sinh thêm tem mới khi có lý do hợp lệ và ghi nhận audit log (FR-PCK-005)."
    )
    public BaseResponse<ExtraQrResponse> generateExtraQrCodes(
            @PathParam("batchId") Long batchId,
            @Valid ExtraQrRequest request) {
        return packagingFacade.generateExtraQrCodes(
                batchId, request, resolveActorId(), resolveActorName(), resolveClientIp());
    }

    private Long resolveActorId() {
        Principal principal = securityContext != null ? securityContext.getUserPrincipal() : null;
        if (principal instanceof UserSecurityDetailsPrincipal details) {
            return details.getId();
        }
        return null;
    }

    private String resolveActorName() {
        Principal principal = securityContext != null ? securityContext.getUserPrincipal() : null;
        return principal != null ? principal.getName() : null;
    }

    private String resolveClientIp() {
        if (httpRequest == null) return null;
        String forwarded = httpRequest.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return httpRequest.getRemoteAddr();
    }
}
