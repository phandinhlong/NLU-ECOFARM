package jakarta.ecofarm_backend.modules.ver.facade;

import jakarta.ecofarm_backend.modules.ver.dto.ProofBundleResponse;
import jakarta.ecofarm_backend.modules.ver.dto.TraceResponse;
import jakarta.ecofarm_backend.modules.ver.dto.VerVerifyResponse;

public interface TraceFacade {

    TraceResponse getTrace(String code);

    VerVerifyResponse verifyTrace(String code);

    ProofBundleResponse getProofBundle(String code);
}
