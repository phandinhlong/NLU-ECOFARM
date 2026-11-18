package jakarta.ecofarm_backend.common.dto.request;

import jakarta.validation.constraints.Positive;
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
public class AuditLogParam {
    @Positive
    private Long actorId;
    private String action;
    private String entity;
    @Positive
    private Long entityId;
    private String detail;
    private String ipAddr;
}
