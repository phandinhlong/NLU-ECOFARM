package jakarta.ecofarm_backend.common.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReassignApproverRequest {

    @NotNull
    @Positive
    private Long engineerId;
}
