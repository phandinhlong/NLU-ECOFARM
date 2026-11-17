package jakarta.ecofarm_backend.common.dto.response;

/** Projection phản hồi cho một bản ghi QC. */
public record QcResponse(Long id, Long batchId, Double brix, Double avgWeightKg, Boolean passed, String status,
        Long inspectedBy, Long inspectedAt, String sizeDescription, String note,
        Double thresholdBrixUsed, Integer minIsolationDaysUsed, Long isolationEligibleAt,
        Long harvestedAtUsed, Boolean superseded) {
}
