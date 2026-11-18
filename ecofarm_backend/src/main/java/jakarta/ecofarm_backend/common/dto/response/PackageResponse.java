package jakarta.ecofarm_backend.common.dto.response;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageResponse {
    @Positive
    private Long id;
    @Positive
    private Long batchId;
    private String packageCode;
    private Integer unitCount;
    @Positive
    private Long packedBy;
    @Positive
    private Long packedAt;
}
