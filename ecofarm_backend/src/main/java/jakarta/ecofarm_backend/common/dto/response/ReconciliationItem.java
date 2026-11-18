package jakarta.ecofarm_backend.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationItem {
    Long batchId;
    String batchCode;
    String dbRoot;
    String privateRoot;
    String publicRoot;
    String result;
}
