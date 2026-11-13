package jakarta.ecofarm_backend.modules.catalog.rest;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.ExceptionResponse;
import jakarta.ecofarm_backend.common.dto.response.PageResponse;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.exception.ConflictException;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Certificate;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.CertificateRepository;
import jakarta.ecofarm_backend.modules.catalog.dto.CertificateRequest;
import jakarta.ecofarm_backend.modules.catalog.dto.CertificateResponse;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Path("/v1/certificates")
@Stateless
@RequireRoles({"ADMIN", "ENGINEER"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CertificateResource {

    @Inject
    private CertificateRepository certificateRepository;

    @GET
    public BaseResponse<PageResponse<CertificateResponse>> list(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("greenhouseId") Long greenhouseId,
            @QueryParam("expiringInDays") Integer expiringInDays) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        // Số ngày cảnh báo phải dương; số âm/0 hoặc không truyền thì bỏ qua bộ lọc hết hạn.
        Integer expiring = (expiringInDays == null || expiringInDays <= 0) ? null : expiringInDays;

        long total = certificateRepository.countActive(greenhouseId, expiring);
        List<CertificateResponse> content = certificateRepository
                .findPage(safePage, safeSize, greenhouseId, expiring)
                .stream()
                .map(CertificateResponse::from)
                .toList();
        return BaseResponse.build(new PageResponse<>(content, safePage, safeSize, total));
    }

    @POST
    public Response create(@Valid CertificateRequest request) {
        if (!certificateRepository.existsGreenhouse(request.getGreenhouseId())) {
            throw badRequest(ErrorCode.GREENHOUSE_NOT_FOUND);
        }

        LocalDate from;
        LocalDate to;
        try {
            from = LocalDate.parse(request.getValidFrom());
            to = LocalDate.parse(request.getValidTo());
        } catch (DateTimeParseException e) {
            throw badRequest(ErrorCode.INVALID_DATE);
        }
        if (to.isBefore(from)) {
            throw badRequest(ErrorCode.INVALID_DATE);
        }

        if (certificateRepository.existsByCertNo(request.getCertNo())) {
            throw new ConflictException(ErrorCode.CERT_NO_EXISTS);
        }

        Certificate certificate = new Certificate();
        certificate.setGreenhouseId(request.getGreenhouseId());
        certificate.setCertType(request.getCertType());
        certificate.setCertNo(request.getCertNo().trim());
        certificate.setValidFrom(from);
        certificate.setValidTo(to);
        certificate.setFileKey(request.getFileKey() == null || request.getFileKey().isBlank()
                ? null : request.getFileKey().trim());
        certificate.setActive(true);

        certificateRepository.persist(certificate);
        return Response.status(Response.Status.CREATED)
                .entity(BaseResponse.build(CertificateResponse.from(certificate)))
                .build();
    }

    private WebApplicationException badRequest(ErrorCode errorCode) {
        return new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                .entity(ExceptionResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .isSuccess(false)
                        .build())
                .build());
    }
}
