package jakarta.ecofarm_backend.modules.catalog.rest;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.PageResponse;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.exception.ConflictException;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Variety;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.VarietyRepository;
import jakarta.ecofarm_backend.modules.catalog.dto.VarietyRequest;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/v1/varieties")
@Stateless
@RequireRoles({"ADMIN", "ENGINEER"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VarietyResource {

    @Inject
    private VarietyRepository varietyRepository;

    @GET
    public BaseResponse<PageResponse<Variety>> list(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("q") String q) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);

        long total = varietyRepository.countActive(q);
        List<Variety> content = varietyRepository.findPage(safePage, safeSize, q);
        return BaseResponse.build(new PageResponse<>(content, safePage, safeSize, total));
    }

    @GET
    @Path("/{id}")
    public BaseResponse<Variety> detail(@PathParam("id") Long id) {
        Variety variety = varietyRepository.findById(id);
        if (variety == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return BaseResponse.build(variety);
    }

    @POST
    public Response create(@Valid VarietyRequest request) {
        if (varietyRepository.existsByCode(request.getCode())) {
            throw new ConflictException(ErrorCode.CODE_EXISTS);
        }

        Variety variety = new Variety();
        applyRequest(variety, request);

        varietyRepository.persist(variety);
        return Response.status(Response.Status.CREATED).entity(BaseResponse.build(variety)).build();
    }

    @PUT
    @Path("/{id}")
    public BaseResponse<Variety> update(@PathParam("id") Long id, @Valid VarietyRequest request) {
        Variety variety = varietyRepository.findById(id);
        if (variety == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        if (request.getVersion() != null && !request.getVersion().equals(variety.getVersion())) {
            throw new ConflictException(ErrorCode.VERSION_CONFLICT);
        }

        boolean codeChanged = !variety.getCode().equals(request.getCode());
        if (codeChanged && varietyRepository.countBatches(id) > 0) {
            throw new ConflictException(ErrorCode.CATALOG_CODE_LOCKED);
        }
        if (codeChanged && varietyRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new ConflictException(ErrorCode.CODE_EXISTS);
        }

        applyRequest(variety, request);
        Variety updated = varietyRepository.merge(variety);
        updated.setBatchCount(varietyRepository.countBatches(id));
        return BaseResponse.build(updated);
    }

    private void applyRequest(Variety variety, VarietyRequest request) {
        variety.setCode(request.getCode().trim());
        variety.setName(request.getName().trim());
        variety.setDaysPollinationToHarvest(request.getDaysPollinationToHarvest());
        variety.setMinBrix(request.getMinBrix());
        variety.setMinIsolationDays(request.getMinIsolationDays());
    }
}
