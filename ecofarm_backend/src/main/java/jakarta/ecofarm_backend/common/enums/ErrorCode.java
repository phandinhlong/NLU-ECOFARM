package jakarta.ecofarm_backend.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(1000, "User not found"),
    JWT_INVALID(1002, "Jwt invalid"),
    PASSWORD_OR_EMAIL_INVALID(1003, "Password or email dont match"),
    REQUIRE_ACTIVE_USER(1004, "User must active account"),
    PASSWORD_NOT_MATCH(1005, "Confirm password does not match"),
    EMAIL_ALREADY_EXISTS(1006, "Email already exists"),
    ROLE_NOT_FOUND(1007, "Role not found"),
    PERMISSION_DENIED(1008, "Permission denied"),
    BATCH_NOT_FOUND(2001, "Batch not found"),
    ACTIVATION_PRECONDITION_FAILED(2002, "Batch is not eligible for activation"),
    BATCH_NOT_ACTIVE(2003, "Batch must be ACTIVE to print QR codes"),
    QR_NOT_FOUND(2004, "QR code not found"),
    QR_ALREADY_GENERATED(2005, "QR codes already generated for this batch; use reprint=true"),
    EXTRA_QR_REASON_REQUIRED(2006, "Reason is required when generating extra QR codes"),
    INVALID_SERIAL_RANGE(2007, "Invalid serial range"),
    PACKAGE_NOT_FOUND(2008, "Package not found for batch"),
    OPTIMISTIC_LOCK_CONFLICT(2009, "Batch version conflict");

    private final Integer code;
    private final String message;
}
