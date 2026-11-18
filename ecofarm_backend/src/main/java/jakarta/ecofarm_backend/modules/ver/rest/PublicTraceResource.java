package jakarta.ecofarm_backend.modules.ver.rest;

import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.PublicTrace;
import jakarta.ecofarm_backend.common.dto.response.VerifyResult;
import jakarta.ecofarm_backend.modules.ver.facade.TraceFacade;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

// Khong @Consumes(JSON) de POST verify khong can body (FE goi khong kem Content-Type)
@Path("/trace")
@Tag(name = "Public", description = "Tra cứu và xác thực công khai, không cần đăng nhập")
@Produces(MediaType.APPLICATION_JSON)
public class PublicTraceResource {

    @Inject
    private TraceFacade traceFacade;

    @GET
    @Path("/{code}")
    @Operation(
            summary = "Hành trình rút gọn của mã truy xuất",
            description = "Trả về thông tin lô, chỉ số, timeline, chứng nhận và trạng thái blockchain; không chứa PII hay sản lượng."
    )
    public BaseResponse<PublicTrace> getTrace(@PathParam("code") String code) {
        return traceFacade.getTrace(code);
    }

    @POST
    @Path("/{code}/verify")
    @Operation(
            summary = "Xác thực Merkle root với blockchain",
            description = "Dựng lại Merkle root từ sự kiện đã neo và đối chiếu với root trên Polygon Amoy hoặc private chain Besu."
    )
    public BaseResponse<VerifyResult> verifyTrace(@PathParam("code") String code) {
        return traceFacade.verifyTrace(code);
    }
}
