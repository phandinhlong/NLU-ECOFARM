package jakarta.ecofarm_backend.modules.blockchain.rest;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import jakarta.ecofarm_backend.common.dto.BatchInfo;
import jakarta.ecofarm_backend.common.dto.BlockchainStatus;
import jakarta.ecofarm_backend.common.dto.CertificateInfo;
import jakarta.ecofarm_backend.common.dto.PublicTrace;
import jakarta.ecofarm_backend.common.dto.RecallInfo;
import jakarta.ecofarm_backend.common.dto.ShipmentInfo;
import jakarta.ecofarm_backend.common.dto.TimelineEvent;
import jakarta.ecofarm_backend.common.dto.TraceMetrics;
import jakarta.ecofarm_backend.config.BlockchainConfig;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Certificate;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.EventAttachment;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Greenhouse;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.MerkleSnapshot;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.ProductPackage;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.QcRecord;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.QrCode;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Recall;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Shipment;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Variety;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.AnchorRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.BatchRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.MerkleSnapshotRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.RecallRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.TraceLookupRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/trace")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublicTraceResource {

    @Inject
    private TraceLookupRepository traceRepository;

    @Inject
    private BatchRepository batchRepository;

    @Inject
    private MerkleSnapshotRepository merkleSnapshotRepository;

    @Inject
    private AnchorRepository anchorRepository;

    @Inject
    private RecallRepository recallRepository;

    @Inject
    private BlockchainConfig blockchainConfig;

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

        trace.setTimeline(buildTimeline(batch.getId()));
        trace.setShipment(buildShipment(batch.getId()));
        trace.setBlockchain(buildBlockchainStatus(batch.getId()));
        trace.setRecall(buildRecall(batch.getId()));

        return trace;
    }

    private List<TimelineEvent> buildTimeline(Long batchId) {
        List<CultivationEvent> events = traceRepository.findCultivationEventsByBatchId(batchId);
        if (events.isEmpty()) return List.of();

        List<Long> eventIds = events.stream().map(CultivationEvent::getId).toList();
        Map<Long, List<EventAttachment>> attachmentsByEvent = traceRepository
                .findAttachmentsByEventIds(eventIds)
                .stream()
                .collect(Collectors.groupingBy(EventAttachment::getEventId));

        return events.stream().map(event -> {
            TimelineEvent item = new TimelineEvent();
            item.setOccurredAt(event.getRecordedAt());
            item.setEventType(event.getEventType());
            item.setTitle(event.getEventType());
            item.setDescription(event.getEventType() + " #" + event.getSeq());
            List<EventAttachment> attachments = attachmentsByEvent.get(event.getId());
            if (attachments != null && !attachments.isEmpty()) {
                item.setPhotoUrl(attachments.get(0).getFileKey());
            }
            return item;
        }).toList();
    }

    private ShipmentInfo buildShipment(Long batchId) {
        Optional<Shipment> shipment = traceRepository.findLatestShipmentByBatchId(batchId);
        if (shipment.isEmpty()) return null;
        ShipmentInfo info = new ShipmentInfo();
        info.setDestination(shipment.get().getDestination());
        info.setShippedAt(shipment.get().getShippedAt());
        return info;
    }

    private BlockchainStatus buildBlockchainStatus(Long batchId) {
        MerkleSnapshot snapshot = merkleSnapshotRepository.findByBatchId(batchId);
        if (snapshot == null) {
            return new BlockchainStatus(false, null, null, null, null, null);
        }
        Optional<Anchor> anchor = anchorRepository.findLatestConfirmedBySnapshotId(snapshot.getId());
        if (anchor.isEmpty()) {
            return new BlockchainStatus(false, null, snapshot.getMerkleRoot(), null, null, null);
        }
        Anchor a = anchor.get();
        Integer chainId = a.getChainId();
        boolean isPublic = blockchainConfig.getPublicChainId() != null
                && blockchainConfig.getPublicChainId().equals(chainId);
        String chainName = isPublic ? "Polygon Amoy" : "Private Chain (Besu)";
        return new BlockchainStatus(true, chainName, snapshot.getMerkleRoot(),
                a.getTxHash(), a.getAnchoredAt(), a.getExplorerUrl());
    }

    private RecallInfo buildRecall(Long batchId) {
        List<Recall> recalls = recallRepository.findByBatchId(batchId);
        if (recalls.isEmpty()) {
            RecallInfo info = new RecallInfo();
            info.setRecalled(false);
            return info;
        }
        RecallInfo info = new RecallInfo();
        info.setRecalled(true);
        info.setReason(recalls.get(0).getReason());
        return info;
    }

    private CertificateInfo toCertificateInfo(Certificate certificate) {
        CertificateInfo info = new CertificateInfo();
        info.setCertType(certificate.getCertType());
        info.setCertNo(certificate.getCertNo());
        info.setValidTo(certificate.getValidTo() == null ? null : certificate.getValidTo().toString());
        return info;
    }
}
