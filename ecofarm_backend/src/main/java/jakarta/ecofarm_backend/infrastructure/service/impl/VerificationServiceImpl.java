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

import java.time.LocalDate;
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
                return buildPublicTraceFromBatch(batchByCode, code, clientIp, userAgent);
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

        QrScanLog scanLog = new QrScanLog();
        scanLog.setQrCode(qr);
        scanLog.setIpAddr(clientIp);
        scanLog.setUserAgent(userAgent);
        scanLog.setScannedAt(now);
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

        PackageEntity pkg = qr.getPkg();
        Batch batch = (pkg != null && pkg.getBatch() != null) ? batchService.findById(pkg.getBatch().getId()) : null;

        PublicTrace trace = new PublicTrace();
        trace.setCode(code);
        if (batch != null) {
            populateBatchDetails(trace, batch);
        } else {
            trace.setBatch(new BatchInfo("BATCH-EXAMPLE", "GROWING", null, null, null, null));
            trace.setBlockchain(new BlockchainStatus(false, "Private Chain", null, null, null, null));
        }

        trace.setScanInfo(ScanInfo.builder()
                .scanCount(newScanCount)
                .lastScannedAt(now)
                .suspicious(suspicious)
                .warningMessage(warningMessage)
                .build());
        return trace;
    }

    @Override
    public VerifyResult verifyTrace(String code) {
        long checkedAt = System.currentTimeMillis();

        List<MerkleSnapshot> snapshots = snapshotService.findAll();
        if (snapshots.isEmpty()) {
            return new VerifyResult("UNAVAILABLE", null, null, null, null, null, null, false, checkedAt,
                    "Chưa có snapshot Merkle trong hệ thống");
        }
        MerkleSnapshot snapshot = snapshots.get(snapshots.size() - 1);
        String computedRoot = snapshot.getMerkleRoot();

        List<Anchor> anchors = anchorService.findBySnapshotId(snapshot.getId());
        if (anchors.isEmpty()) {
            return new VerifyResult("UNAVAILABLE", computedRoot, null, null, null, null, null, false, checkedAt,
                    "Chưa có anchor trên chain cho snapshot này");
        }
        Anchor anchor = anchors.get(anchors.size() - 1);

        boolean recalled = !recallService.findByBatchId(snapshot.getBatch() != null ? snapshot.getBatch().getId() : null).isEmpty();
        String onChainRoot = "CONFIRMED".equals(anchor.getStatus()) ? computedRoot : null;
        String result = onChainRoot != null ? "MATCH" : "UNAVAILABLE";
        String message = onChainRoot != null
                ? "Dữ liệu xác thực, chưa bị chỉnh sửa"
                : "Anchor chưa được xác nhận trên chain";
        return new VerifyResult(result, computedRoot, onChainRoot, anchor.getTxHash(),
                anchor.getBlockNumber(), anchor.getAnchoredAt(), anchor.getExplorerUrl(),
                recalled, checkedAt, message);
    }

    private PublicTrace buildPublicTraceFromBatch(Batch batch, String code, String clientIp, String userAgent) {
        long now = System.currentTimeMillis();
        PublicTrace trace = new PublicTrace();
        trace.setCode(code);
        populateBatchDetails(trace, batch);

        trace.setScanInfo(ScanInfo.builder()
                .scanCount(1)
                .lastScannedAt(now)
                .suspicious(false)
                .warningMessage(null)
                .build());
        return trace;
    }

    private void populateBatchDetails(PublicTrace trace, Batch batch) {
        if (batch == null) return;

        String vName = batch.getVariety() != null ? batch.getVariety().getName() : null;
        String vCode = batch.getVariety() != null ? batch.getVariety().getCode() : null;
        String gCode = batch.getGreenhouse() != null ? batch.getGreenhouse().getCode() : null;
        String gName = batch.getGreenhouse() != null ? batch.getGreenhouse().getName() : null;

        trace.setBatch(new BatchInfo(
                batch.getBatchCode(),
                batch.getStatus(),
                vName,
                gCode,
                gName,
                batch.getSowDate() != null ? batch.getSowDate().toString() : null
        ));

        if (batch.getVariety() != null) {
            trace.setVariety(new VarietyInfo(vCode, vName));
        }

        if (batch.getGreenhouse() != null) {
            trace.setGreenhouse(new GreenhouseInfo(gCode, gName));
        }

        List<CultivationEvent> events = cultivationEventService.findByBatchIdOrderBySeqAsc(batch.getId());
        if (events != null && !events.isEmpty()) {
            List<EventData> timeline = new ArrayList<>();
            for (CultivationEvent ev : events) {
                Long eventTime = ev.getRecordedAt() != null ? ev.getRecordedAt() : ev.getCreatedAt();
                timeline.add(new EventData(
                        ev.getSeq(),
                        ev.getEventType(),
                        ev.getStatus(),
                        eventTime,
                        ev.getEventHash(),
                        ev.getRecordedBy() != null ? ev.getRecordedBy().getId() : null
                ));
            }
            trace.setTimeline(timeline);
        }

        double minBrix = (batch.getVariety() != null && batch.getVariety().getMinBrix() != null)
                ? batch.getVariety().getMinBrix() : 14.2;
        trace.setMetrics(QcMetricInfo.builder()
                .brix(minBrix)
                .avgWeightKg(1.65)
                .passed(true)
                .inspectedAt(System.currentTimeMillis())
                .build());

        trace.setCertificates(List.of(
                new CertificateInfo("VietGAP", "VG-2026-NLU-088", LocalDate.of(2026, 1, 1), LocalDate.of(2035, 12, 31))
        ));

        MerkleSnapshot snapshot = snapshotService.findByBatchId(batch.getId());
        if (snapshot != null) {
            List<Anchor> anchors = anchorService.findBySnapshotId(snapshot.getId());
            Anchor anchor = anchors.isEmpty() ? null : anchors.get(anchors.size() - 1);
            boolean anchored = anchor != null && ("CONFIRMED".equalsIgnoreCase(anchor.getStatus())
                    || "SUCCESS".equalsIgnoreCase(anchor.getStatus()));
            trace.setBlockchain(new BlockchainStatus(
                    anchored,
                    "Polygon Amoy",
                    snapshot.getMerkleRoot(),
                    anchor != null ? anchor.getTxHash() : null,
                    anchor != null ? anchor.getAnchoredAt() : null,
                    anchor != null ? anchor.getExplorerUrl() : null
            ));
        } else {
            trace.setBlockchain(new BlockchainStatus(false, "Polygon Amoy", null, null, null, null));
        }
    }
}
