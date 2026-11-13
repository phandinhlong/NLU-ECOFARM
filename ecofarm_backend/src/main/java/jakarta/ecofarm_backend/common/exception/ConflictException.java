package jakarta.ecofarm_backend.common.exception;

import jakarta.ecofarm_backend.common.enums.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ConflictException extends RuntimeException {
    private final Integer code;
    private final String message;
    private final List<String> errors;

    public ConflictException(ErrorCode errorCode) {
        super(errorCode.name());
        this(errorCode, Collections.emptyList());
    }

    public ConflictException(ErrorCode errorCode, List<String> errors) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.errors = errors == null ? Collections.emptyList() : errors;
    }
}
