package jakarta.ecofarm_backend.modules.batch.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WorkerSummaryResponse {

    private Long id;
    private String fullName;
    private String email;
    private List<String> roles;
}