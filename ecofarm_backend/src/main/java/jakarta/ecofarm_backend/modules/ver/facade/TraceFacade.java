package jakarta.ecofarm_backend.modules.ver.facade;

import jakarta.ecofarm_backend.common.dto.response.ProofBundleResponse;
import jakarta.ecofarm_backend.common.dto.response.PublicTrace;
import jakarta.ecofarm_backend.common.dto.response.VerifyResult;

public interface TraceFacade {

    PublicTrace getTrace(String code);

    VerifyResult verifyTrace(String code);

    ProofBundleResponse getProofBundle(String code);
}
