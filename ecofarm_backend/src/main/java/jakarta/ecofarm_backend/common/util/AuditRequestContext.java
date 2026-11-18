package jakarta.ecofarm_backend.common.util;

import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RequestScoped
public class AuditRequestContext {
    @Positive
    private Long actorId;
    @Positive
    private Long entityId;
}
