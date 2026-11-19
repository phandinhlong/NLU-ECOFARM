package jakarta.ecofarm_backend.common.dto.response;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BatchDetailResponse {

    private Long id;
    private String batchCode;
    private String status;
    private Boolean locked;
    @Positive
    private Long greenhouseId;
    private String greenhouseCode;
    private String greenhouseName;
    @Positive
    private Long varietyId;
    private String varietyName;
    private Integer daysPollinationToHarvest;
    private String sowDate;
    private String harvestDate;
    private LocalDateTime pollinationDate;
    private String expectedHarvestDate;
    private Long createdBy;
    private LocalDateTime activatedAt;
    @Positive
    private Long workerCount;
    @Positive
    private Long eventCount;
    @Positive
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}