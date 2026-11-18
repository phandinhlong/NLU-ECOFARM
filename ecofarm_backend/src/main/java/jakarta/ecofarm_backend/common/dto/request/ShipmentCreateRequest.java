package jakarta.ecofarm_backend.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentCreateRequest {

    @NotBlank
    @Size(max = 255)
    private String destination;

    @NotBlank
    @Size(max = 20)
    private String vehiclePlate;

    @Positive
    private Long shippedAt;

    @NotNull
    @Positive
    private Integer quantity;
}
