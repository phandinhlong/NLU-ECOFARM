package jakarta.ecofarm_backend.common.dto.response;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GreenhouseResponse {
    @Positive
    private Long id;
    private String code;
    private String name;
    private Double gpsLat;
    private Double gpsLng;
    private Double areaM2;
    private String status;
    @Positive
    private Long batchCount;
    private Boolean active;
    @Positive
    private Long version;
    @Positive
    private Long createdAt;
    @Positive
    private Long updatedAt;
}
