package jakarta.ecofarm_backend.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageResponse {
    private Long id;
    private Long batchId;
    private String packageCode;
    private Integer unitCount;
    private Long packedBy;
    private Long packedAt;
}
