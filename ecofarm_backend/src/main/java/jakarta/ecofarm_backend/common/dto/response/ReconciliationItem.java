package jakarta.ecofarm_backend.common.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReconciliationItem {
    Long batchId;
    String batchCode;
    String dbRoot;
    String privateRoot;
    String publicRoot;
    String result;

}
