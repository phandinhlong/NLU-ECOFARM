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
    CODE_EXISTS(1009, "Catalog code already exists"),
    CATALOG_CODE_LOCKED(1010, "Cannot change code while referenced by a batch"),
    VERSION_CONFLICT(1011, "Data was changed by someone else, please reload"),
    INVALID_DATE(1012, "Invalid effective date"),
    GREENHOUSE_NOT_FOUND(1013, "Greenhouse not found"),
    CERT_NO_EXISTS(1014, "Certificate number already exists"),
    CATALOG_DELETE_LOCKED(1015, "Cannot delete because it is referenced"),
    CERTIFICATE_NOT_FOUND(1016, "Certificate not found"),
    BATCH_NOT_FOUND(2001, "Batch not found"),
    ACTIVATION_PRECONDITION_FAILED(2002, "Batch is not eligible for activation"),
    BATCH_NOT_ACTIVE(2003, "Batch must be ACTIVE to print QR codes"),
    QR_NOT_FOUND(2004, "QR code not found"),
    QR_ALREADY_GENERATED(2005, "QR codes already generated for this batch; use reprint=true"),
    EXTRA_QR_REASON_REQUIRED(2006, "Reason is required when generating extra QR codes"),
    INVALID_SERIAL_RANGE(2007, "Invalid serial range"),
    PACKAGE_NOT_FOUND(2008, "Package not found for batch"),
    OPTIMISTIC_LOCK_CONFLICT(2009, "Batch version conflict"),
    BATCH_LOCKED(2010, "Batch data is locked; workers cannot be assigned"),
    BATCH_NOT_ASSIGNED(2011, "You are not assigned to this batch"),
    VARIETY_NOT_FOUND(2012, "Variety not found"),
    INVALID_WORKER(2013, "Invalid worker"),
    EVENT_NOT_FOUND(4001, "Event not found"),
    EVENT_SCHEMA_INVALID(4002, "Event data is invalid"),
    EVENT_ORDER_INVALID(4003, "Event order is invalid"),
    EVENT_ALREADY_REVIEWED(4004, "Event has already been reviewed"),
    EVENT_ALREADY_SUPERSEDED(4005, "Event has already been amended"),
    WORKER_INVALID_ROLE(4006, "Worker role is invalid"),
    WORKER_NOT_ASSIGNED(4007, "Worker is not assigned to this batch"),
    EVENT_APPROVER_NOT_ASSIGNED(4008, "You are not the assigned approver"),
    TASK_NOT_FOUND(4009, "Task not found"),
    TASK_ALREADY_COMPLETED(4010, "Task is already completed or cancelled"),
    ANCHOR_FAILED(4011, "Failed to anchor data on blockchain"),
    ANCHOR_NOT_FOUND(3001, "Anchor not found"),
    ANCHOR_NOT_RETRYABLE(3002, "Anchor is not in FAILED state; cannot retry");

    private final Integer code;
    private final String message;
}
