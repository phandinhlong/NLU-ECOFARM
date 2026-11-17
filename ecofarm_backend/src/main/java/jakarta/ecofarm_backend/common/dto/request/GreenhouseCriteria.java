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

    @QueryParam("q")
    private String q;

    @QueryParam("lastIndex")
    @Positive
    private Long lastIndex;

    public Optional<String> getStatusFilter() {
        return Optional.ofNullable(status).filter(value -> !value.isBlank());
    }

    public Optional<String> getKeywordFilter() {
        return Optional.ofNullable(q).filter(value -> !value.isBlank());
    }

    public Optional<Long> getLastIndexFilter() {
        return Optional.ofNullable(lastIndex).filter(value -> value > 0);
    }
}
