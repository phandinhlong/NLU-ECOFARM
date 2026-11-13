package jakarta.ecofarm_backend.modules.packaging.facade;

import jakarta.ecofarm_backend.common.dto.request.ActivateBatchRequest;
import jakarta.ecofarm_backend.common.dto.request.ExtraQrRequest;
import jakarta.ecofarm_backend.common.dto.response.ActivateResponse;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.ExtraQrResponse;

public interface PackagingFacade {

    BaseResponse<ActivateResponse> activateBatch(
            Long batchId,
            ActivateBatchRequest request,
            Long actorId,
            String actorName,
            String ipAddr
    );

    byte[] exportQrLabels(
            Long batchId,
            boolean reprint,
            Integer fromSerial,
            Integer toSerial,
            String format,
            Long actorId,
            String actorName,
            String ipAddr
    );

    BaseResponse<ExtraQrResponse> generateExtraQrCodes(
            Long batchId,
            ExtraQrRequest request,
            Long actorId,
            String actorName,
            String ipAddr
    );
}
