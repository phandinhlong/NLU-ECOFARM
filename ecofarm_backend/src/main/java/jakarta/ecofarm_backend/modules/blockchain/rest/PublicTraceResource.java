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
import jakarta.ecofarm_backend.common.dto.VerifyResult;
import jakarta.ecofarm_backend.common.util.MerkleUtil;
import jakarta.ecofarm_backend.config.BlockchainConfig;
import jakarta.ecofarm_backend.modules.blockchain.PolygonClient;
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
import org.eclipse.microprofile.config.inject.ConfigProperty;

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

    @Inject
    private PolygonClient polygonClient;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.public.explorer.base-url",
            defaultValue = "https://amoy.polygonscan.com/tx/")
    private String explorerBaseUrl;

    @GET
    @Path("/{code}")
    public PublicTrace getPublicTrace(@PathParam("code") String code) {
        Batch batch = resolveBatch(code);

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

    @POST
    @Path("/{code}/verify")
    public VerifyResult verifyTrace(@PathParam("code") String code) {
        Batch batch = resolveBatch(code);

        VerifyResult result = new VerifyResult();
        result.setCheckedAt(System.currentTimeMillis());

        List<Recall> recalls = recallRepository.findByBatchId(batch.getId());
        result.setRecalled(!recalls.isEmpty());
        if (!recalls.isEmpty()) {
            result.setRecallReason(recalls.get(0).getReason());
        }

        MerkleSnapshot snapshot = merkleSnapshotRepository.findByBatchId(batch.getId());
        if (snapshot == null) {
            result.setResult("UNAVAILABLE");
            result.setMessage("Lô hàng chưa có dữ liệu neo chuỗi (chưa tạo Merkle snapshot).");
            return result;
        }

        Optional<Anchor> anchor = anchorRepository.findLatestConfirmedBySnapshotId(snapshot.getId());
        if (anchor.isEmpty()) {
            result.setResult("UNAVAILABLE");
            result.setMessage("Chưa có giao dịch neo chuỗi nào được xác nhận cho lô hàng này.");
            return result;
        }
        Anchor confirmedAnchor = anchor.get();
        result.setTxHash(confirmedAnchor.getTxHash());
        result.setBlockNumber(confirmedAnchor.getBlockNumber());
        result.setAnchoredAt(confirmedAnchor.getAnchoredAt());
        String explorerUrl = confirmedAnchor.getExplorerUrl();
        if (explorerUrl == null || explorerUrl.isBlank()) {
            explorerUrl = explorerBaseUrl + confirmedAnchor.getTxHash();
        }
        result.setExplorerUrl(explorerUrl);

        List<String> leaves = MerkleUtil.parseLeaves(
                merkleSnapshotRepository.findLeavesJson(snapshot.getId()));
        String computedRoot = MerkleUtil.buildRoot(leaves);
        result.setComputedRoot(computedRoot);

        try {
            Map<String, Object> txInfo = polygonClient.getTransactionInfo(confirmedAnchor.getTxHash());
            Object payload = txInfo.get("decodedPayload");
            String onChainRoot = payload == null ? null : payload.toString();
            if (onChainRoot == null || onChainRoot.isBlank()) {
                result.setResult("UNAVAILABLE");
                result.setMessage("Không đọc được dữ liệu neo trên chuỗi (giao dịch chưa được mine hoặc không tồn tại trên Polygon Amoy).");
                return result;
            }
            result.setOnChainRoot(onChainRoot);
            result.setResult(rootsMatch(computedRoot, onChainRoot) ? "MATCH" : "MISMATCH");
            result.setMessage("MATCH".equals(result.getResult())
                    ? "Dữ liệu trên chuỗi khớp với hành trình của lô hàng."
                    : "Dữ liệu trên chuỗi KHÔNG khớp với hành trình của lô hàng.");
        } catch (Exception e) {
            result.setResult("UNAVAILABLE");
            result.setMessage("Không thể xác thực lúc này (Polygon RPC lỗi): " + e.getMessage());
        }
        return result;
    }

    private Batch resolveBatch(String code) {
        QrCode qrCode = traceRepository.findQrCodeByCode(code)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        ProductPackage productPackage = traceRepository.findPackageById(qrCode.getPackageId())
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return batchRepository.findById(productPackage.getBatchId())
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private static boolean rootsMatch(String computedRoot, String onChainRoot) {
        if (computedRoot == null || onChainRoot == null) return false;
        String a = computedRoot.trim();
        String b = onChainRoot.trim();
        if (a.startsWith("0x") || a.startsWith("0X")) a = a.substring(2);
        if (b.startsWith("0x") || b.startsWith("0X")) b = b.substring(2);
        return a.equalsIgnoreCase(b);
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
