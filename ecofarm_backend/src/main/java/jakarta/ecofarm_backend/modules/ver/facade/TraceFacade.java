package jakarta.ecofarm_backend.modules.ver.facade;

import jakarta.ecofarm_backend.common.dto.response.ProofBundleResponse;
import jakarta.ecofarm_backend.common.dto.response.TraceResponse;
import jakarta.ecofarm_backend.common.dto.response.VerVerifyResponse;

public interface TraceFacade {

    TraceResponse getTrace(String code);

    VerVerifyResponse verifyTrace(String code);

    ProofBundleResponse getProofBundle(String code);
}
