package jakarta.ecofarm_backend.common.dto.response;

/** Projection phản hồi cho trạng thái lô sau thu hoạch/QC. */
public record BatchResponse(Long id, String batchCode, Long varietyId, String status,
        Long harvestedAt, String harvestDate) {
}
