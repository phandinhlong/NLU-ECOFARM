package jakarta.ecofarm_backend.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExceptionResponse {
    private Integer code;
    private String message;
    private Boolean isSuccess;
    private List<String> errors;
}
