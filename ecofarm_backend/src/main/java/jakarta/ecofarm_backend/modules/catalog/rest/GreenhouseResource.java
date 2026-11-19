package jakarta.ecofarm_backend.modules.catalog.rest;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.request.GreenhouseCriteria;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.modules.catalog.dto.GreenhouseRequest;
import jakarta.ecofarm_backend.modules.catalog.dto.GreenhouseResponse;
import jakarta.ecofarm_backend.modules.catalog.facade.GreenhouseFacade;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1/greenhouses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GreenhouseResource {

    @Inject
    private GreenhouseFacade greenhouseFacade;

    @GET
    @RequireRoles({"ADMIN", "ENGINEER", "FARMER", "PACKER", "QUALITY_CONTROL", "USER"})
    public BaseResponse<CursorPageResponse<GreenhouseResponse>> findGreenhouses(@BeanParam GreenhouseCriteria criteria) {
        return greenhouseFacade.findGreenhouses(criteria);
    }

    @GET
    @Path("/statistics")
    @RequireRoles({"ADMIN", "ENGINEER", "FARMER", "PACKER", "QUALITY_CONTROL", "USER"})
    public BaseResponse<Long> countGreenhouses(@BeanParam GreenhouseCriteria criteria) {
        return greenhouseFacade.countGreenhouses(criteria);
    }

    @GET
    @Path("/{id}")
    @RequireRoles({"ADMIN", "ENGINEER", "FARMER", "PACKER", "QUALITY_CONTROL", "USER"})
    public BaseResponse<GreenhouseResponse> findGreenhouse(@PathParam("id") Long id) {
        return greenhouseFacade.findGreenhouse(id);
    }

    @POST
    @RequireRoles({"ADMIN", "ENGINEER"})
    public Response createGreenhouse(@Valid GreenhouseRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(greenhouseFacade.createGreenhouse(request))
                .build();
    }

    @PUT
    @Path("/{id}")
    @RequireRoles({"ADMIN", "ENGINEER"})
    public BaseResponse<GreenhouseResponse> updateGreenhouse(@PathParam("id") Long id, @Valid GreenhouseRequest request) {
        return greenhouseFacade.updateGreenhouse(id, request);
    }

    @DELETE
    @Path("/{id}")
    @RequireRoles({"ADMIN", "ENGINEER"})
    public BaseResponse<Boolean> deleteGreenhouse(@PathParam("id") Long id) {
        return greenhouseFacade.deleteGreenhouse(id);
    }
}
