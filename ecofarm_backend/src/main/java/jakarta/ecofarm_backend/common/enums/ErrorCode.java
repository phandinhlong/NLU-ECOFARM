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
    CATALOG_DELETE_LOCKED(1015, "Không thể xóa vì đã có dữ liệu tham chiếu"),
    CERTIFICATE_NOT_FOUND(1016, "Chứng nhận không tồn tại"),
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
    QC_SAMPLE_COUNT_INVALID(2014, "Độ Brix và cân nặng phải nhập đúng 5 mẫu"),
    QC_SAMPLE_VALUE_INVALID(2015, "Giá trị mẫu Brix hoặc cân nặng không hợp lệ"),
    QC_ISOLATION_NOT_MET(2016, "Thời gian cách ly chưa đủ; không thể đánh giá đạt"),
    QC_MAX_ATTEMPTS_EXCEEDED(2017, "Lô đã vượt quá số lần QC tối đa (3 lần) và đã bị hủy"),
    QC_BATCH_STATUS_INVALID(2018, "Lô không ở trạng thái hợp lệ để thực hiện kiểm định QC"),
    QC_RECORD_NOT_FOUND(2019, "Không tìm thấy bản ghi QC"),
    QC_ADMIN_NOTE_REQUIRED(2020, "Ghi chú của Admin là bắt buộc khi duyệt QC"),
    QC_ALREADY_SUPERSEDED(2021, "Bản ghi QC này đã bị thay thế hoặc đã được xử lý"),
    PRE_HARVEST_QC_REQUIRED(2028, "Lô chưa đạt kiểm định chất lượng trước thu hoạch; không được phép thu hoạch"),
    ANCHOR_NOT_FOUND(3001, "Anchor not found"),
    ANCHOR_NOT_RETRYABLE(3002, "Anchor is not in FAILED state; cannot retry"),
    SNAPSHOT_NOT_FOUND(3003, "Merkle snapshot not found for this batch");


    private final Integer code;
    private final String message;
}
