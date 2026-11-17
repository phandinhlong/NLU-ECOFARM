package jakarta.ecofarm_backend.modules.blockchain.facade.impl;

import jakarta.ecofarm_backend.common.dto.response.PublicTrace;
import jakarta.ecofarm_backend.common.dto.response.VerifyResult;
import jakarta.ecofarm_backend.modules.blockchain.facade.VerificationFacade;
import jakarta.ecofarm_backend.infrastructure.service.VerificationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;

@ApplicationScoped
public class VerificationFacadeImpl implements VerificationFacade {

    @Inject
    private VerificationService verificationService;

    @Inject
    private HttpServletRequest servletRequest;

    @Override
    public PublicTrace getPublicTrace(String code) {
        String clientIp = resolveClientIp();
        String userAgent = servletRequest != null ? servletRequest.getHeader("User-Agent") : null;
        return this.verificationService.getPublicTrace(code, clientIp, userAgent);
    }

    @Override
    public PublicTrace getPublicTrace(String code, String clientIp, String userAgent) {
        return this.verificationService.getPublicTrace(code, clientIp, userAgent);
    }

    @Override
    public VerifyResult verifyTrace(String code) {
        return this.verificationService.verifyTrace(code);
    }

    private String resolveClientIp() {
        if (servletRequest == null) return null;
        String forwarded = servletRequest.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return servletRequest.getRemoteAddr();
    }
}
