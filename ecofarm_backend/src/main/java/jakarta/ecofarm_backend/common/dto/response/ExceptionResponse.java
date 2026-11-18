package jakarta.ecofarm_backend.common.dto.response;

import lombok.*;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionResponse {
    private Integer code;
    private String errorCode;
    private String message;
    private Boolean isSuccess;
    private List<String> errors;
}
