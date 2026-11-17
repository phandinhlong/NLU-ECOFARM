package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.common.dto.response.PublicTrace;
import jakarta.ecofarm_backend.common.dto.response.VerifyResult;

public interface VerificationService {

    PublicTrace getPublicTrace(String code, String clientIp, String userAgent);

    VerifyResult verifyTrace(String code);
}
