package jakarta.ecofarm_backend.modules.catalog.facade.impl;

import jakarta.ecofarm_backend.common.dto.request.CertificateCriteria;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.exception.ConflictException;
import jakarta.ecofarm_backend.common.exception.EntityNotFoundException;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Certificate;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Greenhouse;
import jakarta.ecofarm_backend.infrastructure.service.CertificateService;
import jakarta.ecofarm_backend.infrastructure.service.GreenhouseService;
import jakarta.ecofarm_backend.common.dto.request.CertificateRequest;
import jakarta.ecofarm_backend.common.dto.response.CertificateResponse;
import jakarta.ecofarm_backend.modules.catalog.facade.CertificateFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@ApplicationScoped
public class CertificateFacadeImpl implements CertificateFacade {

    @Inject
    private CertificateService certificateService;

    @Inject
    private GreenhouseService greenhouseService;

    @Override
    public BaseResponse<CursorPageResponse<CertificateResponse>> findCertificates(CertificateCriteria criteria) {
        List<Certificate> certificates = certificateService.findPage(criteria);
        boolean hasMore = certificates.size() > CertificateCriteria.LENGTH;
        List<Certificate> page = hasMore ? certificates.subList(0, CertificateCriteria.LENGTH) : certificates;

        List<CertificateResponse> content = page.stream().map(this::toResponse).toList();

        Long lastId = page.isEmpty() ? null : page.get(page.size() - 1).getId();
        return BaseResponse.build(new CursorPageResponse<>(content, lastId, hasMore));
    }

    @Override
    public BaseResponse<Long> countCertificates(CertificateCriteria criteria) {
        return BaseResponse.build(certificateService.countActive(criteria));
    }

    @Override
    @Transactional
    public BaseResponse<CertificateResponse> createCertificate(CertificateRequest request) {
        if (certificateService.existsByCertNo(request.getCertNo().trim())) {
            throw new ConflictException(ErrorCode.CERT_NO_EXISTS);
        }

        Certificate certificate = new Certificate();
        applyRequest(certificate, request);
        certificateService.save(certificate);
        return BaseResponse.build(toResponse(certificate));
    }

    @Override
    @Transactional
    public BaseResponse<CertificateResponse> updateCertificate(Long id, CertificateRequest request) {
        Certificate certificate = certificateService.findById(id);
        if (certificate == null) {
            throw new EntityNotFoundException(ErrorCode.CERTIFICATE_NOT_FOUND);
        }
        if (request.getVersion() != null && !request.getVersion().equals(certificate.getVersion())) {
            throw new ConflictException(ErrorCode.VERSION_CONFLICT);
        }
        if (certificateService.existsByCertNoAndIdNot(request.getCertNo().trim(), id)) {
            throw new ConflictException(ErrorCode.CERT_NO_EXISTS);
        }

        applyRequest(certificate, request);
        Certificate updated = certificateService.save(certificate);
        return BaseResponse.build(toResponse(updated));
    }

    private void applyRequest(Certificate certificate, CertificateRequest request) {
        Greenhouse greenhouse = greenhouseService.findById(request.getGreenhouseId());
        if (greenhouse == null) {
            throw new EntityNotFoundException(ErrorCode.GREENHOUSE_NOT_FOUND);
        }

        LocalDate from;
        LocalDate to;
        try {
            from = LocalDate.parse(request.getValidFrom());
            to = LocalDate.parse(request.getValidTo());
        } catch (DateTimeParseException e) {
            throw new ConflictException(ErrorCode.INVALID_DATE);
        }
        if (to.isBefore(from)) {
            throw new ConflictException(ErrorCode.INVALID_DATE);
        }

        certificate.setGreenhouse(greenhouse);
        certificate.setCertType(request.getCertType());
        certificate.setCertNo(request.getCertNo().trim());
        certificate.setValidFrom(from);
        certificate.setValidTo(to);
        certificate.setFileKey(request.getFileKey() == null || request.getFileKey().isBlank()
                ? null : request.getFileKey().trim());
        if (request.getActive() != null) {
            certificate.setActive(request.getActive());
        }
    }

    private CertificateResponse toResponse(Certificate certificate) {
        return CertificateResponse.builder()
                .id(certificate.getId())
                .greenhouseId(certificate.getGreenhouse() == null ? null : certificate.getGreenhouse().getId())
                .certType(certificate.getCertType())
                .certNo(certificate.getCertNo())
                .validFrom(certificate.getValidFrom() == null ? null : certificate.getValidFrom().toString())
                .validTo(certificate.getValidTo() == null ? null : certificate.getValidTo().toString())
                .fileKey(certificate.getFileKey())
                .active(certificate.isActive())
                .version(certificate.getVersion())
                .createdAt(certificate.getCreatedAt())
                .updatedAt(certificate.getUpdatedAt())
                .downloadUrl(null)
                .build();
    }
}
