package jakarta.ecofarm_backend.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class VarietyResponse {
    private Long id;
    private String code;
    private String name;
    private Integer daysPollinationToHarvest;
    private Double minBrix;
    private Integer minIsolationDays;
    private Long batchCount;
    private Boolean active;
    private Long version;
    private Long createdAt;
    private Long updatedAt;
}
