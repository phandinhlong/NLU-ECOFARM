package jakarta.ecofarm_backend.common.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentReceiveRequest {

    @NotNull
    private Boolean accepted;

    @Positive
    private Long receivedAt;

    @Size(max = 2000)
    private String conditionNote;

    @Size(max = 5)
    private List<String> photoUrls;
}
