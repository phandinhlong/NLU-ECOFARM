package jakarta.ecofarm_backend.modules.catalog.dto;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Greenhouse;
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

    public static GreenhouseResponse from(Greenhouse g) {
        return GreenhouseResponse.builder()
                .id(g.getId())
                .code(g.getCode())
                .name(g.getName())
                .gpsLat(g.getGpsLat())
                .gpsLng(g.getGpsLng())
                .areaM2(g.getAreaM2())
                .status(g.getStatus())
                .batchCount(g.getBatchCount())
                .active(g.isActive())
                .version(g.getVersion())
                .createdAt(g.getCreatedAt())
                .updatedAt(g.getUpdatedAt())
                .build();
    }
}
