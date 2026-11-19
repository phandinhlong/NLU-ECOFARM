package jakarta.ecofarm_backend.common.dto.response;

import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QcResponse {
    @Positive
    private Long id;
    @Positive
    private Long eventId;
    @Positive
    private Long batchId;
    private String batchCode;
    @Positive
    private Double avgBrix;
    @Positive
    private Double avgWeightKg;
    private Boolean passed;
    private String status;
    @Positive
    private Integer attemptNumber;
    private String failureReason;
    @Positive
    private Long quarantineDaysActual;
    @Positive
    private Integer quarantineDaysRequired;
    private Integer quarantineDaysDeficit;
    @Positive
    private List<Double> brixSamples;
    @Positive
    private List<Double> weightSamples;
    private List<String> photoHashes;
    @Positive
    private Long inspectedBy;
    @Positive
    private Long inspectedAt;
    private String actorRef;
    private String clientEventId;
    private String eventHash;
    private String adminDecision;
    private String adminNote;
    @Positive
    private Long reviewedBy;
    @Positive
    private Long reviewedAt;
    private Boolean systemSuggestedPassed;
    private String expectedHarvestDate;
    @Positive
    private Long daysSinceSowing;
    private Map<String, Object> criteriaEvaluation;
    private String message;
}
