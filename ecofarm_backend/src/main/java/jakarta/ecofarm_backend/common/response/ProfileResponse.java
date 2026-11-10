package jakarta.ecofarm_backend.common.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Builder
public class ProfileResponse {
    private Long id;
    private String email;
    private String fullName;
}
