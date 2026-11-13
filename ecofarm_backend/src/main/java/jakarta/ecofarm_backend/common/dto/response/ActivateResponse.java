package jakarta.ecofarm_backend.common.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivateResponse {
    private Long batchId;
    private String status;

    @JsonProperty("package")
    private PackageResponse packageInfo;

    private Long snapshotId;
    private String merkleRoot;
    private Integer leafCount;
    private String statusUrl;
}
