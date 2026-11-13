package jakarta.ecofarm_backend.modules.catalog.rest;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.PageResponse;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.exception.ConflictException;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Greenhouse;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.GreenhouseRepository;
import jakarta.ecofarm_backend.modules.catalog.dto.GreenhouseRequest;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/v1/greenhouses")
@Stateless
@RequireRoles({"ADMIN", "ENGINEER"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GreenhouseResource {

    @Inject
    private GreenhouseRepository greenhouseRepository;

    @GET
    public BaseResponse<PageResponse<Greenhouse>> list(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("status") String status,
            @QueryParam("q") String q) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);

        long total = greenhouseRepository.countActive(status, q);
        List<Greenhouse> content = greenhouseRepository.findPage(safePage, safeSize, status, q);
        return BaseResponse.build(new PageResponse<>(content, safePage, safeSize, total));
    }

    @GET
    @Path("/{id}")
    public BaseResponse<Greenhouse> detail(@PathParam("id") Long id) {
        Greenhouse greenhouse = greenhouseRepository.findById(id);
        if (greenhouse == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return BaseResponse.build(greenhouse);
    }

    @POST
    public Response create(@Valid GreenhouseRequest request) {
        if (greenhouseRepository.existsByCode(request.getCode())) {
            throw new ConflictException(ErrorCode.CODE_EXISTS);
        }

        Greenhouse greenhouse = new Greenhouse();
        applyRequest(greenhouse, request);

        greenhouseRepository.persist(greenhouse);
        return Response.status(Response.Status.CREATED).entity(BaseResponse.build(greenhouse)).build();
    }

    @PUT
    @Path("/{id}")
    public BaseResponse<Greenhouse> update(@PathParam("id") Long id, @Valid GreenhouseRequest request) {
        Greenhouse greenhouse = greenhouseRepository.findById(id);
        if (greenhouse == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        if (request.getVersion() != null && !request.getVersion().equals(greenhouse.getVersion())) {
            throw new ConflictException(ErrorCode.VERSION_CONFLICT);
        }

        boolean codeChanged = !greenhouse.getCode().equals(request.getCode());
        if (codeChanged && greenhouseRepository.countBatches(id) > 0) {
            throw new ConflictException(ErrorCode.CATALOG_CODE_LOCKED);
        }
        if (codeChanged && greenhouseRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new ConflictException(ErrorCode.CODE_EXISTS);
        }

        applyRequest(greenhouse, request);
        Greenhouse updated = greenhouseRepository.merge(greenhouse);
        updated.setBatchCount(greenhouseRepository.countBatches(id));
        return BaseResponse.build(updated);
    }

    private void applyRequest(Greenhouse greenhouse, GreenhouseRequest request) {
        greenhouse.setCode(request.getCode().trim());
        greenhouse.setName(request.getName().trim());
        greenhouse.setGpsLat(request.getGpsLat() == null ? null : request.getGpsLat().doubleValue());
        greenhouse.setGpsLng(request.getGpsLng() == null ? null : request.getGpsLng().doubleValue());
        greenhouse.setAreaM2(request.getAreaM2() == null ? null : request.getAreaM2().doubleValue());
        greenhouse.setStatus(request.getStatus() == null ? "ACTIVE" : request.getStatus());
    }
}
