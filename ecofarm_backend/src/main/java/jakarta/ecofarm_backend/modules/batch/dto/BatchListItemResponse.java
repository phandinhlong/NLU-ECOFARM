package jakarta.ecofarm_backend.modules.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BatchListItemResponse {

    private Long id;
    private String batchCode;
    private String status;
    private Boolean locked;
    private Long greenhouseId;
    private String greenhouseCode;
    private String greenhouseName;
    private Long varietyId;
    private String varietyName;
    private String sowDate;
    private String harvestDate;
    private Long pollinationDate;
    private String expectedHarvestDate;
    private Long createdBy;
    private Long activatedAt;
    private Long workerCount;
    private Long version;
    private Long createdAt;
    private Long updatedAt;
}