package jakarta.ecofarm_backend.common.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogCriteria extends BaseCriteria {

    @Positive
    @QueryParam("lastIndex")
    private Long lastIndex;

    @Positive
    @QueryParam("actorId")
    private Long actorId;

    @Size(max = 100)
    @QueryParam("action")
    private String action;

    @Size(max = 100)
    @QueryParam("entity")
    private String entity;

    @Positive
    @QueryParam("entityId")
    private Long entityId;

    @Min(0)
    @QueryParam("from")
    private Long from;

    @Min(0)
    @QueryParam("to")
    private Long to;

    public String getAction() {
        return normalize(action);
    }

    public String getEntity() {
        return normalize(entity);
    }

    @AssertTrue(message = "from must be <= to")
    public boolean isFromBeforeTo() {
        return from == null || to == null || from <= to;
    }

    private static String normalize(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
