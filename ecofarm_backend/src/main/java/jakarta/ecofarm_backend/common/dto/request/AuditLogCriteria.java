package jakarta.ecofarm_backend.common.dto.request;

import jakarta.validation.constraints.AssertTrue;
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

    @QueryParam("lastIndex")
    private Long lastIndex;

    @QueryParam("actorId")
    private Long actorId;

    @QueryParam("action")
    private String action;

    @QueryParam("entity")
    private String entity;

    @QueryParam("entityId")
    private Long entityId;

    @QueryParam("from")
    private Long from;

    @QueryParam("to")
    private Long to;

    public String getAction() {
        return normalize(action);
    }

    public String getEntity() {
        return normalize(entity);
    }

    @AssertTrue(message = "Audit filters require positive IDs, action/entity of at most 100 characters, "
            + "and nonnegative epoch milliseconds with from <= to")
    public boolean isValid() {
        return isPositiveOrNull(lastIndex)
                && isPositiveOrNull(actorId)
                && isPositiveOrNull(entityId)
                && (getAction() == null || getAction().length() <= 100)
                && (getEntity() == null || getEntity().length() <= 100)
                && (from == null || from >= 0)
                && (to == null || to >= 0)
                && (from == null || to == null || from <= to);
    }

    private static boolean isPositiveOrNull(Long value) {
        return value == null || value > 0;
    }

    private static String normalize(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
