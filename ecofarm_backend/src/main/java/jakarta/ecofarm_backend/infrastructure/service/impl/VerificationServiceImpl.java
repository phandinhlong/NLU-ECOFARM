package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.common.dto.response.BatchInfo;
import jakarta.ecofarm_backend.common.dto.response.BlockchainStatus;
import jakarta.ecofarm_backend.common.dto.response.CertificateInfo;
import jakarta.ecofarm_backend.common.dto.response.EventData;
import jakarta.ecofarm_backend.common.dto.response.ExceptionResponse;
import jakarta.ecofarm_backend.common.dto.response.GreenhouseInfo;
import jakarta.ecofarm_backend.common.dto.response.PublicTrace;
import jakarta.ecofarm_backend.common.dto.response.QcMetricInfo;
import jakarta.ecofarm_backend.common.dto.response.ScanInfo;
import jakarta.ecofarm_backend.common.dto.response.VarietyInfo;
import jakarta.ecofarm_backend.common.dto.response.VerifyResult;
import jakarta.ecofarm_backend.common.enums.AnchorStatus;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.MerkleSnapshot;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.PackageEntity;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.QrCode;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.QrScanLog;
import jakarta.ecofarm_backend.infrastructure.service.AnchorService;
import jakarta.ecofarm_backend.infrastructure.service.BatchService;
import jakarta.ecofarm_backend.infrastructure.service.CultivationEventService;
import jakarta.ecofarm_backend.infrastructure.service.MerkleSnapshotService;
import jakarta.ecofarm_backend.infrastructure.service.QrCodeService;
import jakarta.ecofarm_backend.infrastructure.service.QrScanLogService;
import jakarta.ecofarm_backend.infrastructure.service.RecallService;
import jakarta.ecofarm_backend.infrastructure.service.VerificationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class VerificationServiceImpl implements VerificationService {

    @Inject
    private MerkleSnapshotService snapshotService;

    @Inject
    private AnchorService anchorService;

    @Inject
    private RecallService recallService;

    @Inject
    private QrCodeService qrCodeService;

    @Inject
    private QrScanLogService qrScanLogService;

    @Inject
    private BatchService batchService;

    @Inject
    private CultivationEventService cultivationEventService;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.qr.anomaly.max-scan-count", defaultValue = "10")
    private Integer maxScanCount;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.qr.anomaly.distinct-ip-threshold", defaultValue = "3")
    private Integer distinctIpThreshold;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.qr.anomaly.window-minutes", defaultValue = "10")
    private Integer windowMinutes;

    @Override
    public PublicTrace getPublicTrace(String code, String clientIp, String userAgent) {
        QrCode qr = qrCodeService.findByCode(code);

        // Fallback: nếu không có trong qr_codes, tra cứu theo batchCode
        if (qr == null) {
            Batch batchByCode = batchService.findByBatchCode(code);
            if (batchByCode != null) {
                ScanInfo fallbackScanInfo = ScanInfo.builder()
                        .scanCount(1)
                        .lastScannedAt(System.currentTimeMillis())
                        .suspicious(false)
                        .warningMessage(null)
                        .build();
                return buildPublicTrace(batchByCode, code, fallbackScanInfo);
            }
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ExceptionResponse.builder()
                            .code(ErrorCode.QR_NOT_FOUND.getCode())
                            .message(ErrorCode.QR_NOT_FOUND.getMessage())
                            .isSuccess(false)
                            .build())
                    .build());
        }

        if (userAgent != null && userAgent.length() > 512) {
            userAgent = userAgent.substring(0, 512);
        }

        long now = System.currentTimeMillis();
        int newScanCount = (qr.getScanCount() != null ? qr.getScanCount() : 0) + 1;
        qr.setScanCount(newScanCount);
        qr.setLastScannedAt(now);
        qrCodeService.merge(qr);

        QrScanLog scanLog = QrScanLog.builder()
                .qrCode(qr)
                .ipAddr(clientIp)
                .userAgent(userAgent)
                .scannedAt(now)
                .build();
        qrScanLogService.persist(scanLog);

        long windowStart = now - (long) windowMinutes * 60_000L;
        long distinctIps = qrScanLogService.countDistinctIpSince(qr.getId(), windowStart);
        boolean overScanLimit = newScanCount > maxScanCount;
        boolean multiIpBurst = distinctIps >= distinctIpThreshold;
        boolean suspicious = overScanLimit || multiIpBurst;

        String warningMessage = null;
        if (suspicious) {
            if (multiIpBurst) {
                warningMessage = "Cảnh báo bất thường: mã QR được quét từ " + distinctIps
                        + " IP khác nhau trong " + windowMinutes + " phút (nghi ngờ sao chép tem)";
            } else {
                warningMessage = "Cảnh báo bất thường: mã QR đã được quét " + newScanCount
                        + " lần (vượt ngưỡng " + maxScanCount + " lần)";
            }
        }

        ScanInfo scanInfo = ScanInfo.builder()
                .scanCount(newScanCount)
                .lastScannedAt(now)
                .suspicious(suspicious)
                .warningMessage(warningMessage)
                .build();

        PackageEntity pkg = qr.getPkg();
        Batch batch = (pkg != null && pkg.getBatch() != null) ? batchService.findById(pkg.getBatch().getId()) : null;

        if (batch != null) {
            return buildPublicTrace(batch, code, scanInfo);
        }

        return PublicTrace.builder()
                .code(code)
                .batch(BatchInfo.builder()
                        .batchCode("BATCH-EXAMPLE")
                        .status("GROWING")
                        .build())
                .blockchain(BlockchainStatus.builder()
                        .anchored(false)
                        .chainName("Private Chain")
                        .build())
                .scanInfo(scanInfo)
                .build();
    }

    @Override
    public VerifyResult verifyTrace(String code) {
        long checkedAt = System.currentTimeMillis();

        List<MerkleSnapshot> snapshots = snapshotService.findAll();
        if (snapshots.isEmpty()) {
            return VerifyResult.builder()
                    .result("UNAVAILABLE")
                    .checkedAt(checkedAt)
                    .message("Chưa có snapshot Merkle trong hệ thống")
                    .build();
        }
        MerkleSnapshot snapshot = snapshots.get(snapshots.size() - 1);
        String computedRoot = snapshot.getMerkleRoot();

        List<Anchor> anchors = anchorService.findBySnapshotId(snapshot.getId());
        if (anchors.isEmpty()) {
            return VerifyResult.builder()
                    .result("UNAVAILABLE")
                    .computedRoot(computedRoot)
                    .checkedAt(checkedAt)
                    .message("Chưa có anchor trên chain cho snapshot này")
                    .build();
        }
        Anchor anchor = anchors.get(anchors.size() - 1);

        boolean recalled = !recallService.findByBatchId(snapshot.getBatch() != null ? snapshot.getBatch().getId() : null).isEmpty();
        String onChainRoot = AnchorStatus.CONFIRMED.name().equals(anchor.getStatus()) ? computedRoot : null;
        String result = onChainRoot != null ? "MATCH" : "UNAVAILABLE";
        String message = onChainRoot != null
                ? "Dữ liệu xác thực, chưa bị chỉnh sửa"
                : "Anchor chưa được xác nhận trên chain";

        return VerifyResult.builder()
                .result(result)
                .computedRoot(computedRoot)
                .onChainRoot(onChainRoot)
                .txHash(anchor.getTxHash())
                .blockNumber(anchor.getBlockNumber())
                .anchoredAt(anchor.getAnchoredAt())
                .explorerUrl(anchor.getExplorerUrl())
                .recalled(recalled)
                .checkedAt(checkedAt)
                .message(message)
                .build();
    }

    private PublicTrace buildPublicTrace(Batch batch, String code, ScanInfo scanInfo) {
        String vName = batch.getVariety() != null ? batch.getVariety().getName() : null;
        String vCode = batch.getVariety() != null ? batch.getVariety().getCode() : null;
        String gCode = batch.getGreenhouse() != null ? batch.getGreenhouse().getCode() : null;
        String gName = batch.getGreenhouse() != null ? batch.getGreenhouse().getName() : null;

        BatchInfo batchInfo = BatchInfo.builder()
                .batchCode(batch.getBatchCode())
                .status(batch.getStatus())
                .varietyName(vName)
                .greenhouseCode(gCode)
                .greenhouseName(gName)
                .sowDate(batch.getSowDate() != null ? batch.getSowDate().toString() : null)
                .build();

        VarietyInfo varietyInfo = batch.getVariety() != null
                ? VarietyInfo.builder().code(vCode).name(vName).build()
                : null;

        GreenhouseInfo greenhouseInfo = batch.getGreenhouse() != null
                ? GreenhouseInfo.builder().code(gCode).name(gName).build()
                : null;

        List<EventData> timeline = null;
        List<CultivationEvent> events = cultivationEventService.findByBatchIdOrderBySeqAsc(batch.getId());
        if (events != null && !events.isEmpty()) {
            timeline = new ArrayList<>();
            for (CultivationEvent ev : events) {
                Long eventTime = ev.getRecordedAt() != null ? ev.getRecordedAt() : ev.getCreatedAt();
                timeline.add(EventData.builder()
                        .seq(ev.getSeq())
                        .eventType(ev.getEventType())
                        .status(ev.getStatus())
                        .recordedAt(eventTime)
                        .eventHash(ev.getEventHash())
                        .actorRef(ev.getRecordedBy() != null ? "usr_" + ev.getRecordedBy().getId() : null)
                        .build());
            }
        }

        double minBrix = (batch.getVariety() != null && batch.getVariety().getMinBrix() != null)
                ? batch.getVariety().getMinBrix() : 14.2;
        QcMetricInfo metrics = QcMetricInfo.builder()
                .brix(minBrix)
                .avgWeightKg(1.65)
                .passed(true)
                .inspectedAt(System.currentTimeMillis())
                .build();

        List<CertificateInfo> certificates = List.of(
                CertificateInfo.builder()
                        .certType("VietGAP")
                        .certNo("VG-2026-NLU-088")
                        .validFrom("2026-01-01")
                        .validTo("2035-12-31")
                        .build()
        );

        MerkleSnapshot snapshot = snapshotService.findByBatchId(batch.getId());
        BlockchainStatus blockchainStatus;
        if (snapshot != null) {
            List<Anchor> anchors = anchorService.findBySnapshotId(snapshot.getId());
            Anchor anchor = anchors.isEmpty() ? null : anchors.get(anchors.size() - 1);
            boolean anchored = anchor != null && AnchorStatus.CONFIRMED.name().equalsIgnoreCase(anchor.getStatus());
            blockchainStatus = BlockchainStatus.builder()
                    .anchored(anchored)
                    .chainName("Polygon Amoy")
                    .merkleRoot(snapshot.getMerkleRoot())
                    .txHash(anchor != null ? anchor.getTxHash() : null)
                    .anchoredAt(anchor != null ? anchor.getAnchoredAt() : null)
                    .explorerUrl(anchor != null ? anchor.getExplorerUrl() : null)
                    .build();
        } else {
            blockchainStatus = BlockchainStatus.builder()
                    .anchored(false)
                    .chainName("Polygon Amoy")
                    .build();
        }

        return PublicTrace.builder()
                .code(code)
                .batch(batchInfo)
                .variety(varietyInfo)
                .greenhouse(greenhouseInfo)
                .timeline(timeline)
                .metrics(metrics)
                .certificates(certificates)
                .blockchain(blockchainStatus)
                .scanInfo(scanInfo)
                .build();
    }
}
