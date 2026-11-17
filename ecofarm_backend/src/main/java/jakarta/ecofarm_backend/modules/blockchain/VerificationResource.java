package jakarta.ecofarm_backend.modules.blockchain;

import jakarta.ecofarm_backend.common.dto.response.PublicTrace;
import jakarta.ecofarm_backend.common.dto.response.VerifyResult;
import jakarta.ecofarm_backend.modules.blockchain.facade.VerificationFacade;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/v1/block-chain")
@Tag(name = "Verification", description = "Tra cứu nguồn gốc công khai và xác thực dữ liệu chuỗi")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VerificationResource {

    @Inject
    private VerificationFacade verificationFacade;

    @GET
    @Path("/public/trace/{code}")
    @Operation(summary = "Tra cứu nguồn gốc công khai", description = "Truy xuất thông tin lô hàng, chứng nhận VietGAP, nhật ký canh tác và bằng chứng neo blockchain qua mã tem QR hoặc mã lô")
    public PublicTrace getPublicTrace(@PathParam("code") String code) {
        return this.verificationFacade.getPublicTrace(code);
    }

    @POST
    @Path("/public/trace/{code}/verify")
    @Operation(summary = "Đối soát xác thực chuỗi", description = "Kiểm tra tính toàn vẹn của dữ liệu bằng cách so khớp Merkle root được tính toán với Merkle root đã neo on-chain")
    public VerifyResult verifyTrace(@PathParam("code") String code) {
        return this.verificationFacade.verifyTrace(code);
    }
}
