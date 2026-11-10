package jakarta.ecofarm_backend.common.dto.response;

import lombok.*;

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
