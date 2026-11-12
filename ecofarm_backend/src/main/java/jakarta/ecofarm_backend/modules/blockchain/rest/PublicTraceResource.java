package jakarta.ecofarm_backend.modules.blockchain.rest;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import jakarta.ecofarm_backend.common.dto.BatchInfo;
import jakarta.ecofarm_backend.common.dto.CertificateInfo;
import jakarta.ecofarm_backend.common.dto.PublicTrace;
import jakarta.ecofarm_backend.common.dto.TraceMetrics;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Certificate;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Greenhouse;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.ProductPackage;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.QcRecord;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.QrCode;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Variety;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.BatchRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.TraceLookupRepository;

import java.util.List;
import java.util.Optional;

@Path("/trace")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublicTraceResource {

    @Inject
    private TraceLookupRepository traceRepository;

    @Inject
    private BatchRepository batchRepository;

    @GET
    @Path("/{code}")
    public PublicTrace getPublicTrace(@PathParam("code") String code) {
        QrCode qrCode = traceRepository.findQrCodeByCode(code)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        ProductPackage productPackage = traceRepository.findPackageById(qrCode.getPackageId())
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        Batch batch = batchRepository.findById(productPackage.getBatchId())
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

        PublicTrace trace = new PublicTrace();
        trace.setCode(code);

        BatchInfo batchInfo = new BatchInfo();
        batchInfo.setBatchCode(batch.getBatchCode());
        batchInfo.setStatus(batch.getStatus());
        batchInfo.setSowDate(batch.getSowDate() == null ? null : batch.getSowDate().toString());
        batchInfo.setHarvestDate(batch.getHarvestDate() == null ? null : batch.getHarvestDate().toString());

        Optional<Variety> variety = batch.getVarietyId() == null
                ? Optional.empty()
                : traceRepository.findVarietyById(batch.getVarietyId());
        variety.ifPresent(v -> batchInfo.setVarietyName(v.getName()));

        Optional<Greenhouse> greenhouse = batch.getGreenhouseId() == null
                ? Optional.empty()
                : traceRepository.findGreenhouseById(batch.getGreenhouseId());
        greenhouse.ifPresent(gh -> {
            batchInfo.setGreenhouseCode(gh.getCode());
            batchInfo.setGreenhouseName(gh.getName());

            List<CertificateInfo> certificates = traceRepository.findCertificatesByGreenhouseId(gh.getId())
                    .stream()
                    .map(this::toCertificateInfo)
                    .toList();
            trace.setCertificates(certificates);
        });
        trace.setBatch(batchInfo);

        List<QcRecord> qcRecords = traceRepository.findQcRecordsByBatchId(batch.getId());
        if (!qcRecords.isEmpty()) {
            QcRecord latest = qcRecords.get(0);
            TraceMetrics metrics = new TraceMetrics();
            metrics.setBrix(latest.getBrix());
            metrics.setAvgWeightKg(latest.getAvgWeightKg());
            trace.setMetrics(metrics);
        }

        return trace;
    }

    private CertificateInfo toCertificateInfo(Certificate certificate) {
        CertificateInfo info = new CertificateInfo();
        info.setCertType(certificate.getCertType());
        info.setCertNo(certificate.getCertNo());
        info.setValidTo(certificate.getValidTo() == null ? null : certificate.getValidTo().toString());
        return info;
    }
}
