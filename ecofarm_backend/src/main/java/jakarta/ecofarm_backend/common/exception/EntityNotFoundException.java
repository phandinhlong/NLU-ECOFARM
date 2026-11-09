package jakarta.ecofarm_backend.common.exception;

import jakarta.ecofarm_backend.common.enums.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EntityNotFoundException extends RuntimeException{
    private final Integer code;
    private final String message;

    public EntityNotFoundException(ErrorCode errorCode) {
        super(errorCode.name());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
}
