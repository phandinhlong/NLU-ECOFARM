package jakarta.ecofarm_backend.common.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class AssignWorkersRequest {

    @NotNull(message = "Danh sách người lao động không được trống")
    @Size(max = 100, message = "At most 100 workers can be assigned to a batch")
    private List<Long> userIds;
}