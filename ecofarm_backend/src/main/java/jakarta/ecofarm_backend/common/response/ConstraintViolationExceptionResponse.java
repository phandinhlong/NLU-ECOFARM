package jakarta.ecofarm_backend.common.response;

import lombok.*;

import java.time.Instant;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
public class ConstraintViolationExceptionResponse {
    private Map<String, String> details;
    private Boolean isSuccess;
}
