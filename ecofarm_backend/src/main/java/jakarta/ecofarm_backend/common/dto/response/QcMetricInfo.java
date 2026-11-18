package jakarta.ecofarm_backend.common.dto.response;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QcMetricInfo {
    @Positive
    private Double brix;
    @Positive
    private Double avgWeightKg;
    private Boolean passed;
    @Positive
    private Long inspectedAt;
}
