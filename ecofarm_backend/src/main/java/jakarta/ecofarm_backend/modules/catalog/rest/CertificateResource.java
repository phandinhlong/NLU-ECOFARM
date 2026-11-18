package jakarta.ecofarm_backend.modules.catalog.rest;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.request.CertificateCriteria;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.common.dto.request.CertificateRequest;
import jakarta.ecofarm_backend.common.dto.response.CertificateResponse;
import jakarta.ecofarm_backend.modules.catalog.facade.CertificateFacade;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1/certificates")
@RequireRoles({"ADMIN"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CertificateResource {

    @Inject
    private CertificateFacade certificateFacade;

    @GET
    public BaseResponse<CursorPageResponse<CertificateResponse>> findCertificates(@BeanParam CertificateCriteria criteria) {
        return certificateFacade.findCertificates(criteria);
    }

    @GET
    @Path("/statistics")
    public BaseResponse<Long> countCertificates(@BeanParam CertificateCriteria criteria) {
        return certificateFacade.countCertificates(criteria);
    }

    @POST
    public Response createCertificate(@Valid CertificateRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(certificateFacade.createCertificate(request))
                .build();
    }

    @PUT
    @Path("/{id}")
    public BaseResponse<CertificateResponse> updateCertificate(@PathParam("id") Long id, @Valid CertificateRequest request) {
        return certificateFacade.updateCertificate(id, request);
    }
}
