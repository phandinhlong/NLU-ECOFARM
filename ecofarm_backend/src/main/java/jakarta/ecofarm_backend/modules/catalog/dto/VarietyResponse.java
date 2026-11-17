package jakarta.ecofarm_backend.modules.catalog.dto;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.Variety;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class VarietyResponse {
    private Long id;
    private String code;
    private String name;
    private Integer daysPollinationToHarvest;
    private Double minBrix;
    private Integer minIsolationDays;
    private Long batchCount;
    private Boolean active;
    private Long version;
    private Long createdAt;
    private Long updatedAt;

    public static VarietyResponse from(Variety v) {
        return VarietyResponse.builder()
                .id(v.getId())
                .code(v.getCode())
                .name(v.getName())
                .daysPollinationToHarvest(v.getDaysPollinationToHarvest())
                .minBrix(v.getMinBrix())
                .minIsolationDays(v.getMinIsolationDays())
                .batchCount(v.getBatchCount())
                .active(v.isActive())
                .version(v.getVersion())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }
}
