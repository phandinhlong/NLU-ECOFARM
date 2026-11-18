package jakarta.ecofarm_backend.common.exception;

import jakarta.ecofarm_backend.common.enums.ErrorCode;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class UnprocessableEntityException extends RuntimeException {

    private final Integer code;
    private final List<String> errors;

    public UnprocessableEntityException(ErrorCode errorCode) {
        this(errorCode, Collections.emptyList());
    }

    public UnprocessableEntityException(ErrorCode errorCode, List<String> errors) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.errors = errors == null ? Collections.emptyList() : errors;
    }
}
