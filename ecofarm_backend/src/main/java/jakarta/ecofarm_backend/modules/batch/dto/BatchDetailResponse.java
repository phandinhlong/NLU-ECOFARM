package jakarta.ecofarm_backend.modules.batch.dto;

import java.time.LocalDateTime;
import java.util.List;
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
    private Long greenhouseId;
    private String greenhouseCode;
    private String greenhouseName;
    private Long varietyId;
    private String varietyName;
    private Integer daysPollinationToHarvest;
    private String sowDate;
    private String harvestDate;
    private LocalDateTime pollinationDate;
    private String expectedHarvestDate;
    private Long createdBy;
    private LocalDateTime activatedAt;
    private Long workerCount;
    private Long eventCount;
    private List<WorkerSummaryResponse> assignedUsers;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}