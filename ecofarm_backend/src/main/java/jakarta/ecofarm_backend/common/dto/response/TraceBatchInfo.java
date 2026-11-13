package jakarta.ecofarm_backend.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TraceBatchInfo {

    private String batchCode;
    private String status;
    private String sowDate;
    private String harvestDate;
    private Long activatedAt;
    private Boolean active;
    private Boolean recalled;
}
