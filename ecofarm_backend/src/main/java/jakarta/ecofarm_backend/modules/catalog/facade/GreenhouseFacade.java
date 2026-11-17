package jakarta.ecofarm_backend.modules.catalog.facade;

import jakarta.ecofarm_backend.common.dto.request.GreenhouseCriteria;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.modules.catalog.dto.GreenhouseRequest;
import jakarta.ecofarm_backend.modules.catalog.dto.GreenhouseResponse;

public interface GreenhouseFacade {

    BaseResponse<CursorPageResponse<GreenhouseResponse>> findGreenhouses(GreenhouseCriteria criteria);

    BaseResponse<Long> countGreenhouses(GreenhouseCriteria criteria);

    BaseResponse<GreenhouseResponse> findGreenhouse(Long id);

    BaseResponse<GreenhouseResponse> createGreenhouse(GreenhouseRequest request);

    BaseResponse<GreenhouseResponse> updateGreenhouse(Long id, GreenhouseRequest request);
}
