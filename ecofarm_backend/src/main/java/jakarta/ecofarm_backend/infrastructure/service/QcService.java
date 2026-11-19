package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.common.dto.request.HarvestBatchParam;
import jakarta.ecofarm_backend.common.dto.request.RecordQcParam;
import jakarta.ecofarm_backend.common.dto.request.ReviewQcParam;
import jakarta.ecofarm_backend.common.dto.response.HarvestResponse;
import jakarta.ecofarm_backend.common.dto.response.QcResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public interface QcService {

    QcResponse recordQc(@NotNull @Valid RecordQcParam param);

    QcResponse reviewQc(@NotNull @Valid ReviewQcParam param);

    QcResponse getLatestQc(@NotNull @Positive Long batchId);

    HarvestResponse harvestBatch(@NotNull @Valid HarvestBatchParam param);
}
