package jakarta.ecofarm_backend.modules.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GreenhouseResponse {
    private Long id;
    private String code;
    private String name;
    private Double gpsLat;
    private Double gpsLng;
    private Double areaM2;
    private String status;
    private Long batchCount;
    private Boolean active;
    private Long version;
    private Long createdAt;
    private Long updatedAt;
}
