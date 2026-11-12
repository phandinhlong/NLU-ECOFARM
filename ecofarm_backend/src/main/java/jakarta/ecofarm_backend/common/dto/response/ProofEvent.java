package jakarta.ecofarm_backend.common.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProofEvent {
    private Integer seq;
    private String eventType;
    private Object payload;

    public ProofEvent() {}

}
