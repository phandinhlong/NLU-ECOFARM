package jakarta.ecofarm_backend.common.util;

import jakarta.enterprise.context.RequestScoped;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RequestScoped
public class AuditRequestContext {
    private Long actorId;
    private Long entityId;
}
