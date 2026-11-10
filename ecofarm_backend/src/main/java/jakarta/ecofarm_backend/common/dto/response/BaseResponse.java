package jakarta.ecofarm_backend.common.dto.response;

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
@Schema(name = "BaseResponse", description = "Structure of response")
public class BaseResponse<T> {

    private T data;
    private Boolean isSuccess;


    public static <T> BaseResponse<T> build(T data, boolean isSuccess) {
        return (BaseResponse<T>) BaseResponse.builder().isSuccess(isSuccess).data(data).build();
    }

    public static <T> BaseResponse<T> build(T data) {
        return (BaseResponse<T>) BaseResponse.builder().isSuccess(true).data(data).build();
    }

    public static <Void> BaseResponse<Void> ok() {
        return (BaseResponse<Void>) BaseResponse.builder().isSuccess(true).data("Success").build();
    }

}
