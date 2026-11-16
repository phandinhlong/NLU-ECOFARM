package jakarta.ecofarm_backend.common.dto.request;

import java.util.Optional;

import jakarta.ecofarm_backend.common.enums.BatchStatus;
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
    @QueryParam("page")
    private Integer page;

    @QueryParam("size")
    private Integer size;

    @QueryParam("status")
    private BatchStatus status;

    @QueryParam("greenhouseId")
    private Long greenhouseId;

    @QueryParam("batchCode")
    private String batchCodeKeyword;

    private Long mineUserId;

    @QueryParam("mine")
    private Boolean mine;

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

    public int getSafePage() {
        return page == null ? 0 : Math.max(page, 0);
    }

    public int getSafeSize() {
        return size == null || size <= 0 ? 20 : Math.min(size, 100);
    }

    public int getSafeOffset() {
        long offset = (long) getSafePage() * getSafeSize();
        return offset > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) offset;
    }
}
