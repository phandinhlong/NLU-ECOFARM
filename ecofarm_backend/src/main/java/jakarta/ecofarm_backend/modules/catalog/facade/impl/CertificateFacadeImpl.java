package jakarta.ecofarm_backend.modules.catalog.facade.impl;

import jakarta.ecofarm_backend.common.dto.request.CertificateCriteria;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.exception.ConflictException;
import jakarta.ecofarm_backend.common.exception.EntityNotFoundException;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Certificate;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Greenhouse;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.CertificateRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.GreenhouseRepository;
import jakarta.ecofarm_backend.modules.catalog.dto.CertificateRequest;
import jakarta.ecofarm_backend.modules.catalog.dto.CertificateResponse;
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
    private CertificateRepository certificateRepository;

    @Inject
    private GreenhouseRepository greenhouseRepository;

    @Override
    public BaseResponse<CursorPageResponse<CertificateResponse>> findCertificates(CertificateCriteria criteria) {
        List<Certificate> certificates = certificateRepository.findPage(criteria);
        boolean hasMore = certificates.size() > CertificateCriteria.LENGTH;
        List<Certificate> page = hasMore ? certificates.subList(0, CertificateCriteria.LENGTH) : certificates;

        List<CertificateResponse> content = page.stream().map(this::toResponse).toList();

        Long lastId = page.isEmpty() ? null : page.get(page.size() - 1).getId();
        return BaseResponse.build(new CursorPageResponse<>(content, lastId, hasMore));
    }

    @Override
    public BaseResponse<Long> countCertificates(CertificateCriteria criteria) {
        return BaseResponse.build(certificateRepository.countActive(criteria));
    }

    @Override
    @Transactional
    public BaseResponse<CertificateResponse> createCertificate(CertificateRequest request) {
        Greenhouse greenhouse = greenhouseRepository.findById(request.getGreenhouseId());
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

        if (certificateRepository.existsByCertNo(request.getCertNo())) {
            throw new ConflictException(ErrorCode.CERT_NO_EXISTS);
        }

        Certificate certificate = new Certificate();
        certificate.setGreenhouse(greenhouse);
        certificate.setCertType(request.getCertType());
        certificate.setCertNo(request.getCertNo().trim());
        certificate.setValidFrom(from);
        certificate.setValidTo(to);
        certificate.setFileKey(request.getFileKey() == null || request.getFileKey().isBlank()
                ? null : request.getFileKey().trim());

        certificateRepository.save(certificate);
        return BaseResponse.build(toResponse(certificate));
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
