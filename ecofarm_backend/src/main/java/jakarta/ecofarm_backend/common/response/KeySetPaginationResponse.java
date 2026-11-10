package jakarta.ecofarm_backend.common.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Builder
@ToString
@Getter
public class KeySetPaginationResponse<T> {
    private List<T> data;
    private Long lastId;
}
