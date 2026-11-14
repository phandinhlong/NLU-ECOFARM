package jakarta.ecofarm_backend.common.dto.response;

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

    private Double brix;
    private Double avgWeightKg;
    private Boolean passed;
    private Long inspectedAt;
}
