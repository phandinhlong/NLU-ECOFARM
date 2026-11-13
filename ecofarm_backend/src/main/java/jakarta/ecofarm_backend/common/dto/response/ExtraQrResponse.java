package jakarta.ecofarm_backend.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtraQrResponse {
    private Long batchId;
    private Long packageId;
    private Integer previousUnitCount;
    private Integer addedCount;
    private Integer totalUnitCount;
    private Integer fromSerial;
    private Integer toSerial;
    private String reason;
    private List<String> codes;
}
