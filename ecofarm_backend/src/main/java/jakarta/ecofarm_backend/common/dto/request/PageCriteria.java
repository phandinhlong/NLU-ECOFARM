package jakarta.ecofarm_backend.common.dto.request;

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
    private Integer page;
    private Integer size;
    private String status;
    private Long greenhouseId;
    private String q;
    private Long mineUserId;

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
