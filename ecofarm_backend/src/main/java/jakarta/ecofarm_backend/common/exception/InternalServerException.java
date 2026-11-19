package jakarta.ecofarm_backend.common.exception;

import jakarta.ecofarm_backend.common.enums.ErrorCode;
import lombok.Getter;

@Getter
public class InternalServerException extends RuntimeException {

    private final Integer code;
    private final String message;

    public InternalServerException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public InternalServerException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
}
