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
    CODE_EXISTS(1009, "Mã danh mục đã tồn tại"),
    CATALOG_CODE_LOCKED(1010, "Không thể đổi mã khi đã có lô tham chiếu"),
    VERSION_CONFLICT(1011, "Dữ liệu đã bị thay đổi bởi người khác, vui lòng tải lại"),
    INVALID_DATE(1012, "Ngày hiệu lực không hợp lệ"),
    GREENHOUSE_NOT_FOUND(1013, "Nhà màng không tồn tại"),
    CERT_NO_EXISTS(1014, "Số chứng nhận đã tồn tại"),
    BATCH_NOT_FOUND(2001, "Batch not found"),
    ACTIVATION_PRECONDITION_FAILED(2002, "Batch is not eligible for activation"),
    BATCH_NOT_ACTIVE(2003, "Batch must be ACTIVE to print QR codes"),
    QR_NOT_FOUND(2004, "QR code not found"),
    QR_ALREADY_GENERATED(2005, "QR codes already generated for this batch; use reprint=true"),
    EXTRA_QR_REASON_REQUIRED(2006, "Reason is required when generating extra QR codes"),
    INVALID_SERIAL_RANGE(2007, "Invalid serial range"),
    PACKAGE_NOT_FOUND(2008, "Package not found for batch"),
    OPTIMISTIC_LOCK_CONFLICT(2009, "Batch version conflict"),
    EVENT_NOT_FOUND(3001, "Cultivation event not found"),
    ATTACHMENT_NOT_FOUND(3002, "Attachment not found"),
    ATTACHMENT_NOT_IN_BATCH(3003, "Attachment does not belong to this batch"),
    TOO_MANY_ATTACHMENTS(3004, "At most 5 attachments are allowed"),
    ATTACHMENT_NOT_LINKABLE(3005, "Attachment does not exist or is already linked"),
    INVALID_ATTACHMENT(3006, "Invalid attachment"),
    UNSUPPORTED_EVENT_TYPE(3007, "Event type is not supported or cannot be recorded by client"),
    REJECT_REASON_REQUIRED(3008, "A rejection reason is required"),
    HARVEST_FIELDS_REQUIRED(3009, "clientEventId, harvestedAt and estimatedQuantity are required"),
    HARVEST_QUANTITY_INVALID(3010, "estimatedQuantity must be positive"),
    QC_FIELDS_REQUIRED(3011, "brix and avgWeightKg are required"),
    QC_BRIX_OUT_OF_RANGE(3012, "brix out of range [0,30]"),
    QC_AVG_WEIGHT_OUT_OF_RANGE(3013, "avgWeightKg out of range [0.1,20]"),
    QC_MISSING_THRESHOLD(3014, "Variety thresholds (min Brix / isolation days) are not configured"),
    QC_ISOLATION_NOT_ENOUGH(3015, "Isolation window before harvest is not satisfied");

    private final Integer code;
    private final String message;
}
