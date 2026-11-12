package jakarta.ecofarm_backend.modules.blockchain.facade;

import jakarta.ecofarm_backend.common.dto.response.PublicTrace;
import jakarta.ecofarm_backend.common.dto.response.VerifyResult;

public interface VerificationFacade {

    PublicTrace getPublicTrace(String code, String clientIp, String userAgent);

    VerifyResult verifyTrace(String code);
}
