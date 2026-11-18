package jakarta.ecofarm_backend.common.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Optional;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GreenhouseCriteria extends BaseCriteria {

    @QueryParam("status")
    private String status;

    @QueryParam("keyword")
    private String keyword;

    @QueryParam("lastIndex")
    @Positive
    private Long lastId;

    public Optional<String> getStatusFilter() {
        return Optional.ofNullable(status).filter(value -> !value.isBlank());
    }

    public Optional<String> getKeywordFilter() {
        return Optional.ofNullable(keyword).filter(value -> !value.isBlank());
    }

    public Optional<Long> getLastIdFilter() {
        return Optional.ofNullable(lastId).filter(value -> value > 0);
    }
}
