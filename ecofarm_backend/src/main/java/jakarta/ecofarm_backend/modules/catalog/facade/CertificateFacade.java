package jakarta.ecofarm_backend.modules.catalog.facade;

import jakarta.ecofarm_backend.common.dto.request.CertificateCriteria;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.modules.catalog.dto.CertificateRequest;
import jakarta.ecofarm_backend.modules.catalog.dto.CertificateResponse;

public interface CertificateFacade {

    BaseResponse<CursorPageResponse<CertificateResponse>> findCertificates(CertificateCriteria criteria);

    BaseResponse<Long> countCertificates(CertificateCriteria criteria);

    BaseResponse<CertificateResponse> createCertificate(CertificateRequest request);
}
