package jakarta.ecofarm_backend.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class ResetPasswordRequest {
    @NotBlank(message = "Password must not be blank")
    @Size(max = 72, message = "Password must be at most 72 characters")
    private String password;

    @NotBlank(message = "Password must not be blank")
    @Size(max = 72, message = "Password must be at most 72 characters")
    private String confirmPassword;
}
