package jakarta.ecofarm_backend.modules.catalog.facade;

import jakarta.ecofarm_backend.common.dto.request.VarietyCriteria;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.modules.catalog.dto.VarietyRequest;
import jakarta.ecofarm_backend.modules.catalog.dto.VarietyResponse;

public interface VarietyFacade {

    BaseResponse<CursorPageResponse<VarietyResponse>> findVarieties(VarietyCriteria criteria);

    BaseResponse<Long> countVarieties(VarietyCriteria criteria);

    BaseResponse<VarietyResponse> findVariety(Long id);

    BaseResponse<VarietyResponse> createVariety(VarietyRequest request);

    BaseResponse<VarietyResponse> updateVariety(Long id, VarietyRequest request);
}
