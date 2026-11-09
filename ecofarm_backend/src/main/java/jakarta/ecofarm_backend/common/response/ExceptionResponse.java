package jakarta.ecofarm_backend.common.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
public class ExceptionResponse {
    private final Integer code;
    private final String message;
}
