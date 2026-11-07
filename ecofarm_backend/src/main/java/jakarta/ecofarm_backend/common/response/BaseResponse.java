package jakarta.ecofarm_backend.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Builder
@Schema(name = "BaseResponse", description = "Cấu trúc phản hồi chuẩn của hệ thống")
public class BaseResponse<T> {

    @Schema(description = "Dữ liệu trả về của API")
    private T data;

    @Schema(description = "Trạng thái thành công hay thất bại", example = "true")
    private Boolean isSuccess;

    @Schema(description = "Thông báo kết quả", example = "Thành công")
    private String message;

    public static <T> BaseResponse<T> build(T data, boolean isSuccess) {
        return BaseResponse.<T>builder().isSuccess(isSuccess).data(data).build();
    }

    public static <T> BaseResponse<T> build(T data, boolean isSuccess, String message) {
        return BaseResponse.<T>builder().isSuccess(isSuccess).data(data).message(message).build();
    }

    public static <T> BaseResponse<T> ok(T data) {
        return BaseResponse.<T>builder().isSuccess(true).data(data).message("Thành công").build();
    }

    public static BaseResponse<String> ok() {
        return BaseResponse.<String>builder().isSuccess(true).data("Success").message("Thao tác thành công").build();
    }

    public static <T> BaseResponse<T> error(String message) {
        return BaseResponse.<T>builder().isSuccess(false).data(null).message(message).build();
    }
}
