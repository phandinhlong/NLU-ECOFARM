package jakarta.ecofarm_backend.common.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PublicTrace {
    String code;
    BatchInfo batch;
    BlockchainStatus blockchain;
    ScanInfo scanInfo;

}
