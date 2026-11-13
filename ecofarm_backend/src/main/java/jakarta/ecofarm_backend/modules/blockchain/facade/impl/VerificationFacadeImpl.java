package jakarta.ecofarm_backend.modules.blockchain.facade.impl;

import jakarta.ecofarm_backend.common.dto.response.BatchInfo;
import jakarta.ecofarm_backend.common.dto.response.BlockchainStatus;
import jakarta.ecofarm_backend.common.dto.response.ExceptionResponse;
import jakarta.ecofarm_backend.common.dto.response.PublicTrace;
import jakarta.ecofarm_backend.common.dto.response.ScanInfo;
import jakarta.ecofarm_backend.common.dto.response.VerifyResult;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.MerkleSnapshot;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.PackageEntity;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.QrCode;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.QrScanLog;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.AnchorRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.BatchRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.MerkleSnapshotRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.PackageRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.QrCodeRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.QrScanLogRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.RecallRepository;
import jakarta.ecofarm_backend.modules.blockchain.facade.VerificationFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

@ApplicationScoped
public class VerificationFacadeImpl implements VerificationFacade {

    @Inject
    private MerkleSnapshotRepository snapshotRepository;

    @Inject
    private AnchorRepository anchorRepository;

    @Inject
    private RecallRepository recallRepository;

    @Inject
    private QrCodeRepository qrCodeRepository;

    @Inject
    private QrScanLogRepository qrScanLogRepository;

    @Inject
    private PackageRepository packageRepository;

    @Inject
    private BatchRepository batchRepository;

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
        QrCode qr = qrCodeRepository.findByCode(code);
        if (qr == null) {
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
        qr.setUpdatedAt(now);
        qrCodeRepository.merge(qr);

        QrScanLog scanLog = new QrScanLog();
        scanLog.setQrCodeId(qr.getId());
        scanLog.setIpAddr(clientIp);
        scanLog.setUserAgent(userAgent);
        scanLog.setScannedAt(now);
        qrScanLogRepository.persist(scanLog);

        long windowStart = now - (long) windowMinutes * 60_000L;
        long distinctIps = qrScanLogRepository.countDistinctIpSince(qr.getId(), windowStart);
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

        PackageEntity pkg = qr.getPackageId() != null ? packageRepository.findById(qr.getPackageId()) : null;
        Batch batch = (pkg != null && pkg.getBatchId() != null) ? batchRepository.findById(pkg.getBatchId()) : null;

        PublicTrace trace = new PublicTrace();
        trace.setCode(code);
        if (batch != null) {
            trace.setBatch(new BatchInfo(
                    batch.getBatchCode(),
                    batch.getStatus(),
                    null,
                    null,
                    null,
                    batch.getSowDate() != null ? batch.getSowDate().toString() : null
            ));
            MerkleSnapshot snapshot = snapshotRepository.findByBatchId(batch.getId());
            if (snapshot != null) {
                List<Anchor> anchors = anchorRepository.findBySnapshotId(snapshot.getId());
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

        // Batch lookup by code omitted: no qr_codes mapping yet; fall back to latest snapshot.
        List<MerkleSnapshot> snapshots = snapshotRepository.findAll();
        if (snapshots.isEmpty()) {
            return new VerifyResult("UNAVAILABLE", null, null, null, null, null, null, false, checkedAt,
                    "Chưa có snapshot Merkle trong hệ thống");
        }
        MerkleSnapshot snapshot = snapshots.get(snapshots.size() - 1);
        String computedRoot = snapshot.getMerkleRoot();

        List<Anchor> anchors = anchorRepository.findBySnapshotId(snapshot.getId());
        if (anchors.isEmpty()) {
            return new VerifyResult("UNAVAILABLE", computedRoot, null, null, null, null, null, false, checkedAt,
                    "Chưa có anchor trên chain cho snapshot này");
        }
        Anchor anchor = anchors.get(anchors.size() - 1);

        boolean recalled = !recallRepository.findByBatchId(snapshot.getBatchId()).isEmpty();
        String onChainRoot = "CONFIRMED".equals(anchor.getStatus()) ? computedRoot : null;
        String result = onChainRoot != null ? "MATCH" : "UNAVAILABLE";
        String message = onChainRoot != null
                ? "Dữ liệu xác thực, chưa bị chỉnh sửa"
                : "Anchor chưa được xác nhận trên chain";
        return new VerifyResult(result, computedRoot, onChainRoot, anchor.getTxHash(),
                anchor.getBlockNumber(), anchor.getAnchoredAt(), anchor.getExplorerUrl(),
                recalled, checkedAt, message);
    }
}
