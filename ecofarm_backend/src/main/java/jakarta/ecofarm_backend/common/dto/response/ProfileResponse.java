package jakarta.ecofarm_backend.common.dto.response;

import jakarta.ecofarm_backend.common.enums.RoleEnum;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Builder
public class ProfileResponse {
    private Long id;
    private String email;
    private String fullName;
    private List<RoleEnum> role;
}
