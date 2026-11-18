package jakarta.ecofarm_backend.modules.catalog.rest;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.request.VarietyCriteria;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.modules.catalog.dto.VarietyRequest;
import jakarta.ecofarm_backend.modules.catalog.dto.VarietyResponse;
import jakarta.ecofarm_backend.modules.catalog.facade.VarietyFacade;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1/varieties")
@RequireRoles({"ADMIN"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VarietyResource {

    @Inject
    private VarietyFacade varietyFacade;

    @GET
    public BaseResponse<CursorPageResponse<VarietyResponse>> findVarieties(@BeanParam VarietyCriteria criteria) {
        return varietyFacade.findVarieties(criteria);
    }

    @GET
    @Path("/statistics")
    public BaseResponse<Long> countVarieties(@BeanParam VarietyCriteria criteria) {
        return varietyFacade.countVarieties(criteria);
    }

    @GET
    @Path("/{id}")
    public BaseResponse<VarietyResponse> findVariety(@PathParam("id") Long id) {
        return varietyFacade.findVariety(id);
    }

    @POST
    public Response createVariety(@Valid VarietyRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(varietyFacade.createVariety(request))
                .build();
    }

    @PUT
    @Path("/{id}")
    public BaseResponse<VarietyResponse> updateVariety(@PathParam("id") Long id, @Valid VarietyRequest request) {
        return varietyFacade.updateVariety(id, request);
    }
}
