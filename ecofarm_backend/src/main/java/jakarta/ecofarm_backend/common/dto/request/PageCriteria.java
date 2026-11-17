package jakarta.ecofarm_backend.common.dto.request;

import java.util.Optional;

import jakarta.ecofarm_backend.common.enums.BatchStatus;
import jakarta.validation.constraints.Positive;
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
public class PageCriteria extends BaseCriteria {
    @QueryParam("status")
    private BatchStatus status;

    @QueryParam("greenhouseId")
    @Positive
    private Long greenhouseId;

    @QueryParam("batchCode")
    private String batchCodeKeyword;

    @Positive
    private Long mineUserId;

    @QueryParam("mine")
    private Boolean mine;

    @QueryParam("lastIndex")
    @Positive
    private Long lastIndex;

    public static final int LENGTH = 20;

    public Optional<BatchStatus> getStatusFilter() {
        return Optional.ofNullable(status);
    }

    public Optional<String> getBatchCodeKeywordFilter() {
        return Optional.ofNullable(batchCodeKeyword).filter(value -> !value.isBlank());
    }

    public Optional<Long> getGreenhouseFilter() {
        return Optional.ofNullable(greenhouseId);
    }

    public Optional<Long> getMineUserFilter() {
        return Optional.ofNullable(mineUserId);
    }

    public Optional<Long> getLastIndexFilter() {
        return Optional.ofNullable(lastIndex).filter(v -> v > 0);
    }
}
