package jakarta.ecofarm_backend.modules.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogResponse {
    private Long id;
    private Long actorId;
    private String actorName;
    private String actorEmail;
    private String action;
    private String entity;
    private Long entityId;
    private String detail;
    private String ipAddr;
    private Long createdAt;
}
