package jakarta.ecofarm_backend.common.dto.response;

import lombok.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionResponse {
    private Integer code;
    private String message;
    private Boolean isSuccess;
}
