package jakarta.ecofarm_backend.common.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class SignupRequest {
    @NotBlank(message = "Fullname must not be blank")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String fullName;

    @NotBlank(message = "Email must not be blank")
    @Size(max = 255, message = "Email must be at most 255 characters")
    @Email(message = "Email format is invalid")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$",
            message = "Email format is invalid"
    )
    private String email;

    @NotBlank(message = "Password must not be blank")
    @Size(max = 72, message = "Password must be at most 72 characters")
    private String password;

    @NotBlank(message = "Password must not be blank")
    @Size(max = 72, message = "Password must be at most 72 characters")
    private String confirmPassword;
}
