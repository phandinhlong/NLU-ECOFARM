package jakarta.ecofarm_backend.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(1000, "User not found"),
    JWT_INVALID(1002,"Jwt invalid"),
    PASSWORD_OR_EMAIL_INVALID(1003,"Password or email dont match");

    private final Integer code;
    private final String message;
}