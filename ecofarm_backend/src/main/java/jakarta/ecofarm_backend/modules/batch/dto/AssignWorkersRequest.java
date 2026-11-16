package jakarta.ecofarm_backend.modules.batch.dto;

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
    @Size(max = 100, message = "Chỉ được phân công tối đa 100 người lao động cho một lô")
    private List<Long> userIds;
}