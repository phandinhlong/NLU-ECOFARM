package jakarta.ecofarm_backend.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReviewQcRequest {

    @NotBlank(message = "Quyết định không được để trống")
    @Pattern(regexp = "^(APPROVED|REJECTED)$", message = "Quyết định phải là APPROVED hoặc REJECTED")
    private String decision;

    @NotBlank(message = "Ghi chú của Admin là bắt buộc")
    @Size(min = 3, max = 1000, message = "Ghi chú phải có từ 3 đến 1000 ký tự")
    private String note;
}
