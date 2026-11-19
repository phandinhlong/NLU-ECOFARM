package jakarta.ecofarm_backend.common.exception;

import jakarta.ecofarm_backend.common.enums.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidFileException extends RuntimeException {

    private final Integer code;

    public InvalidFileException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public String getMessage() {
        return super.getMessage();
    }
}
