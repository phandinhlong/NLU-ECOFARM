package jakarta.ecofarm_backend.modules.ver.facade;

import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.ProofBundleResponse;
import jakarta.ecofarm_backend.common.dto.response.PublicTrace;
import jakarta.ecofarm_backend.common.dto.response.VerifyResult;

public interface TraceFacade {

    BaseResponse<PublicTrace> getTrace(String code);

    BaseResponse<VerifyResult> verifyTrace(String code);

    BaseResponse<ProofBundleResponse> getProofBundle(String code);
}
