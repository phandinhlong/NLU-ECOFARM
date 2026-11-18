package jakarta.ecofarm_backend.modules.blockchain;

import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.request.RecallRequest;
import jakarta.ecofarm_backend.common.dto.response.AnchorStatus;
import jakarta.ecofarm_backend.common.dto.response.ProofBundle;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Recall;
import jakarta.ecofarm_backend.modules.blockchain.facade.BlockchainFacade;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@Path("/v1/block-chain")
@Tag(name = "Blockchain", description = "Quản lý neo Blockchain, sổ cái và đối soát")
@RequireRoles(value = {"ADMIN", "QUALITY_CONTROL", "FARMER"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BlockchainResource {

    @Inject
    private BlockchainFacade blockchainFacade;

    @GET
    @Path("/admin/anchors")
    @Operation(summary = "Lấy danh sách tất cả các anchor", description = "Danh sách bản ghi neo sổ cái on-chain và private chain")
    public List<Anchor> getAllAnchors() {
        return this.blockchainFacade.getAllAnchors();
    }

    @GET
    @Path("/admin/reconciliation")
    @Operation(summary = "Đối soát dữ liệu", description = "Đối soát Merkle root giữa cơ sở dữ liệu và on-chain")
    public Map<String, Object> getReconciliation(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("onlyMismatch") boolean onlyMismatch
    ) {
        return this.blockchainFacade.getReconciliation(page, size, onlyMismatch);
    }

    @GET
    @Path("/batches/{batchId}/anchor-status")
    @Operation(summary = "Trạng thái neo của lô hàng", description = "Kiểm tra tiến độ neo on-chain, số lượt thử và txHash")
    public AnchorStatus getAnchorStatus(@PathParam("batchId") Long batchId) {
        return this.blockchainFacade.getAnchorStatus(batchId);
    }

    @POST
    @Path("/admin/anchors/{anchorId}/retry")
    @Operation(summary = "Thử lại neo", description = "Đưa anchor FAILED về PENDING để thử lại")
    public Anchor retryAnchor(@PathParam("anchorId") Long anchorId) {
        return this.blockchainFacade.retryAnchor(anchorId);
    }

    @POST
    @Path("/batches/{batchId}/recall")
    @Operation(summary = "Thu hồi lô hàng", description = "Ghi nhận sự kiện thu hồi lô hàng khẩn cấp")
    public Recall recallBatch(@PathParam("batchId") Long batchId, @Valid RecallRequest req) {
        return this.blockchainFacade.recallBatch(batchId, req);
    }

    @GET
    @Path("/batches/{batchId}/proof-bundle")
    @Operation(summary = "Lấy gói chứng minh Merkle", description = "Cung cấp leaf, salt và Merkle proof cho lô hàng")
    public ProofBundle getProofBundle(@PathParam("batchId") Long batchId) {
        return this.blockchainFacade.getProofBundle(batchId);
    }

    @POST
    @Path("/admin/outbox/test")
    @Operation(summary = "Tạo outbox test", description = "Tạo một bản ghi outbox mẫu để kiểm tra anchoring worker")
    public OutboxEvent createTestOutbox(@QueryParam("data") @DefaultValue("EcoFarm Polygon Test") String data) {
        return this.blockchainFacade.createTestOutbox(data);
    }

    @POST
    @Path("/admin/outbox/process")
    @Operation(summary = "Kích hoạt xử lý outbox thủ công", description = "Chạy tiến trình neo ngay lập tức")
    public Map<String, Object> triggerProcessOutbox() {
        return this.blockchainFacade.triggerProcessOutbox();
    }

    @GET
    @Path("/admin/polygon/status")
    @Operation(summary = "Trạng thái kết nối Polygon", description = "Kiểm tra kết nối RPC Polygon Amoy và block number hiện tại")
    public Map<String, Object> testPolygonStatus() throws Exception {
        return this.blockchainFacade.getPolygonStatus();
    }

    @GET
    @Path("/admin/polygon/confirmations/{txHash}")
    @Operation(summary = "Kiểm tra số confirmations", description = "Tra cứu số lượng xác nhận khối cho giao dịch")
    public Map<String, Object> testPolygonConfirmations(@PathParam("txHash") String txHash) throws Exception {
        return this.blockchainFacade.getPolygonConfirmations(txHash);
    }

    @GET
    @Path("/admin/transactions/{txHash}")
    @Operation(summary = "Chi tiết giao dịch", description = "Lấy thông tin biên lai và log giao dịch từ Polygon")
    public Map<String, Object> getTransactionDetail(@PathParam("txHash") String txHash) throws Exception {
        return this.blockchainFacade.getTransactionDetail(txHash);
    }
}
