package jakarta.ecofarm_backend.common.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RecallRequest {
    private String reason;

    public RecallRequest() {}

    public RecallRequest(String reason) {
        this.reason = reason;
    }

}
