package jakarta.ecofarm_backend.common.dto.response;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class VarietyResponse {
    @Positive
    private Long id;
    private String code;
    private String name;
    private Integer daysPollinationToHarvest;
    private Double minBrix;
    private Integer minIsolationDays;
    @Positive
    private Long batchCount;
    private Boolean active;
    @Positive
    private Long version;
    @Positive
    private Long createdAt;
    @Positive
    private Long updatedAt;
}
