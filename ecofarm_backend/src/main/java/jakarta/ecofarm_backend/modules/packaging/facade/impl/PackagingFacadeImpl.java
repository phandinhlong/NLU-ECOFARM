package jakarta.ecofarm_backend.modules.packaging.facade.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ecofarm_backend.common.dto.request.ActivateBatchRequest;
import jakarta.ecofarm_backend.common.dto.request.ExtraQrRequest;
import jakarta.ecofarm_backend.common.dto.response.ActivateResponse;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.ExtraQrResponse;
import jakarta.ecofarm_backend.common.dto.response.PackageResponse;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.exception.ConflictException;
import jakarta.ecofarm_backend.common.exception.EntityNotFoundException;
import jakarta.ecofarm_backend.config.BlockchainConfig;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Anchor;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.AuditLog;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.MerkleSnapshot;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.PackageEntity;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.QrCode;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.AnchorRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.AuditLogRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.BatchRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.CultivationEventRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.MerkleSnapshotRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.OutboxEventRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.PackageRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.QrCodeRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.UserRepository;
import jakarta.ecofarm_backend.modules.packaging.facade.PackagingFacade;
import jakarta.ecofarm_backend.modules.packaging.service.MerkleTreeService;
import jakarta.ecofarm_backend.modules.packaging.service.QrLabelExportService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.web3j.crypto.Hash;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PackagingFacadeImpl implements PackagingFacade {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Inject
    private BatchRepository batchRepository;

    @Inject
    private CultivationEventRepository cultivationEventRepository;

    @Inject
    private PackageRepository packageRepository;

    @Inject
    private QrCodeRepository qrCodeRepository;

    @Inject
    private MerkleSnapshotRepository snapshotRepository;

    @Inject
    private AnchorRepository anchorRepository;

    @Inject
    private OutboxEventRepository outboxEventRepository;

    @Inject
    private AuditLogRepository auditLogRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private MerkleTreeService merkleTreeService;

    @Inject
    private QrLabelExportService qrLabelExportService;

    @Inject
    private BlockchainConfig blockchainConfig;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.frontend.base.url", defaultValue = "http://localhost:3000")
    private String frontendBaseUrl;

    @Override
    @Transactional
    public BaseResponse<ActivateResponse> activateBatch(
            Long batchId,
            ActivateBatchRequest request,
            Long actorId,
            String actorName,
            String ipAddr) {

        Batch batch = batchRepository.findById(batchId);
        if (batch == null) {
            throw new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND);
        }

        if (request.getVersion() != null && batch.getVersion() != null
                && !request.getVersion().equals(batch.getVersion())) {
            throw new ConflictException(ErrorCode.OPTIMISTIC_LOCK_CONFLICT,
                    List.of("Expected version " + batch.getVersion() + " but got " + request.getVersion()));
        }

        List<CultivationEvent> events = cultivationEventRepository.findByBatchIdOrderBySeqAsc(batchId);
        List<String> missing = validateActivationPreconditions(batch, events);
        if (!missing.isEmpty()) {
            throw new ConflictException(ErrorCode.ACTIVATION_PRECONDITION_FAILED, missing);
        }

        long now = System.currentTimeMillis();

        // 1. Tạo Package
        PackageEntity pkg = new PackageEntity();
        pkg.setBatch(batch);
        pkg.setPackageCode("PKG-" + (batch.getBatchCode() != null ? batch.getBatchCode() : batchId) + "-" + now);
        pkg.setUnitCount(request.getUnitCount());
        pkg.setPackedBy(userRepository.findById(actorId).orElse(null));
        pkg.setPackedAt(now);
        packageRepository.persist(pkg);

        // 2. Dựng Merkle tree & lưu MerkleSnapshot
        MerkleTreeService.MerkleResult merkle = merkleTreeService.buildTree(batch.getSaltEnc(), events);
        MerkleSnapshot snapshot = new MerkleSnapshot();
        snapshot.setBatch(batch);
        snapshot.setMerkleRoot(merkle.merkleRoot());
        snapshot.setLeafCount(merkle.leafHashes().size());
        snapshotRepository.persist(snapshot);
        snapshotRepository.updateLeavesJson(snapshot.getId(), merkle.leavesJson());

        // 3. Chuyển lô sang ANCHORING
        batch.setStatus("ANCHORING");
        batch.setActivatedAt(now);
        batchRepository.merge(batch);

        // 4. Tạo Anchor PENDING
        Anchor anchor = new Anchor();
        anchor.setSnapshot(snapshot);
        anchor.setChainId(blockchainConfig.getPublicChainId());
        anchorRepository.persist(anchor);

        // 5. Tạo OutboxEvent finalizeBatch trong cùng transaction
        String batchKey = batch.getBatchCode() != null ? batch.getBatchCode() : String.valueOf(batchId);
        String batchIdHash = Hash.sha3String(batchKey);
        int nextSeq = outboxEventRepository.findNextSeqByBatchIdHash(batchIdHash);
        String eventPayload = buildFinalizePayload(batch, snapshot, pkg, merkle.merkleRoot());
        OutboxEvent outbox = OutboxEvent.builder()
                .batchIdHash(batchIdHash)
                .seq(nextSeq)
                .eventData(eventPayload)
                .status(OutboxEvent.OutboxStatus.PENDING)
                .attempts(0)
                .build();
        outboxEventRepository.persist(outbox);

        // 6. Ghi AuditLog
        auditLogRepository.persist(buildAuditLog(
                actorId,
                "BATCH_ACTIVATE",
                "batches",
                batchId,
                toJson(Map.of(
                        "packageId", pkg.getId(),
                        "packageCode", pkg.getPackageCode(),
                        "unitCount", pkg.getUnitCount(),
                        "snapshotId", snapshot.getId(),
                        "merkleRoot", merkle.merkleRoot(),
                        "leafCount", merkle.leafHashes().size(),
                        "outboxId", outbox.getId()
                )),
                ipAddr
        ));

        PackageResponse pkgResp = PackageResponse.builder()
                .id(pkg.getId())
                .batchId(pkg.getBatch() != null ? pkg.getBatch().getId() : null)
                .packageCode(pkg.getPackageCode())
                .unitCount(pkg.getUnitCount())
                .packedBy(pkg.getPackedBy() != null ? pkg.getPackedBy().getId() : null)
                .packedAt(pkg.getPackedAt())
                .build();

        ActivateResponse response = ActivateResponse.builder()
                .batchId(batchId)
                .status(batch.getStatus())
                .packageInfo(pkgResp)
                .snapshotId(snapshot.getId())
                .merkleRoot(merkle.merkleRoot())
                .leafCount(merkle.leafHashes().size())
                .statusUrl("/api/v1/block-chain/batches/" + batchId + "/anchor-status")
                .build();

        return BaseResponse.build(response);
    }

    @Override
    @Transactional
    public byte[] exportQrLabels(
            Long batchId,
            boolean reprint,
            Integer fromSerial,
            Integer toSerial,
            String format,
            Long actorId,
            String actorName,
            String ipAddr) {

        Batch batch = batchRepository.findById(batchId);
        if (batch == null) {
            throw new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND);
        }

        if (!"ACTIVE".equalsIgnoreCase(batch.getStatus())) {
            throw new ConflictException(ErrorCode.BATCH_NOT_ACTIVE,
                    List.of("Batch status is '" + batch.getStatus() + "', required 'ACTIVE'"));
        }

        PackageEntity pkg = packageRepository.findLatestByBatchId(batchId);
        if (pkg == null) {
            throw new EntityNotFoundException(ErrorCode.PACKAGE_NOT_FOUND);
        }

        if (fromSerial != null && fromSerial < 1
                || toSerial != null && toSerial < 1
                || (fromSerial != null && toSerial != null && fromSerial > toSerial)) {
            throw new ConflictException(ErrorCode.INVALID_SERIAL_RANGE,
                    List.of("Invalid serial range: fromSerial=" + fromSerial + ", toSerial=" + toSerial));
        }

        long now = System.currentTimeMillis();
        int existingCount = qrCodeRepository.countByPackageId(pkg.getId());
        List<QrCode> codesToPrint;

        if (existingCount == 0) {
            int unitCount = pkg.getUnitCount() != null ? pkg.getUnitCount() : 0;
            List<QrCode> generated = generateSerialBatch(pkg, 1, unitCount, "PRINTED", now);
            codesToPrint = filterByRange(generated, fromSerial, toSerial);

            auditLogRepository.persist(buildAuditLog(
                    actorId,
                    "QR_GENERATED",
                    "packages",
                    pkg.getId(),
                    toJson(Map.of(
                            "batchId", batchId,
                            "unitCount", unitCount,
                            "printedCount", codesToPrint.size(),
                            "format", normalizeFormat(format)
                    )),
                    ipAddr
            ));
        } else {
            if (!reprint) {
                throw new ConflictException(ErrorCode.QR_ALREADY_GENERATED,
                        List.of("QR codes already generated (" + existingCount + " labels). Pass reprint=true to reprint."));
            }

            codesToPrint = qrCodeRepository.findByPackageIdOrderBySerialAsc(pkg.getId(), fromSerial, toSerial);
            if (codesToPrint.isEmpty()) {
                throw new ConflictException(ErrorCode.INVALID_SERIAL_RANGE,
                        List.of("No QR codes found in serial range"));
            }

            for (QrCode qr : codesToPrint) {
                int currentReprints = qr.getReprintCount() != null ? qr.getReprintCount() : 0;
                qr.setReprintCount(currentReprints + 1);
                qr.setStatus("PRINTED");
                qr.setPrintedAt(now);
                qrCodeRepository.merge(qr);
            }

            auditLogRepository.persist(buildAuditLog(
                    actorId,
                    "QR_REPRINT",
                    "packages",
                    pkg.getId(),
                    toJson(Map.of(
                            "batchId", batchId,
                            "reprintedCount", codesToPrint.size(),
                            "fromSerial", fromSerial != null ? fromSerial : 1,
                            "toSerial", toSerial != null ? toSerial : existingCount,
                            "format", normalizeFormat(format)
                    )),
                    ipAddr
            ));
        }

        String normalizedFormat = normalizeFormat(format);
        return switch (normalizedFormat) {
            case "zpl" -> qrLabelExportService.exportZpl(batch, codesToPrint, reprint, frontendBaseUrl);
            case "csv" -> qrLabelExportService.exportCsv(batch, codesToPrint, reprint, frontendBaseUrl);
            default -> qrLabelExportService.exportPdf(batch, codesToPrint, reprint, frontendBaseUrl);
        };
    }

    @Override
    @Transactional
    public BaseResponse<ExtraQrResponse> generateExtraQrCodes(
            Long batchId,
            ExtraQrRequest request,
            Long actorId,
            String actorName,
            String ipAddr) {

        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new ConflictException(ErrorCode.EXTRA_QR_REASON_REQUIRED,
                    List.of("reason is required when generating extra QR codes"));
        }

        Batch batch = batchRepository.findById(batchId);
        if (batch == null) {
            throw new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND);
        }

        if (!"ACTIVE".equalsIgnoreCase(batch.getStatus())) {
            throw new ConflictException(ErrorCode.BATCH_NOT_ACTIVE,
                    List.of("Batch status is '" + batch.getStatus() + "', required 'ACTIVE'"));
        }

        PackageEntity pkg = packageRepository.findLatestByBatchId(batchId);
        if (pkg == null) {
            throw new EntityNotFoundException(ErrorCode.PACKAGE_NOT_FOUND);
        }

        long now = System.currentTimeMillis();
        int previousUnitCount = pkg.getUnitCount() != null ? pkg.getUnitCount() : 0;
        int existingCount = qrCodeRepository.countByPackageId(pkg.getId());
        if (existingCount == 0 && previousUnitCount > 0) {
            generateSerialBatch(pkg, 1, previousUnitCount, "GENERATED", now);
        }

        int maxSerial = qrCodeRepository.findMaxSerialByPackageId(pkg.getId());
        int fromSerial = maxSerial + 1;
        int toSerial = maxSerial + request.getExtraCount();

        List<QrCode> extraCodes = generateSerialBatch(pkg, fromSerial, toSerial, "GENERATED", now);

        int newTotalUnitCount = Math.max(previousUnitCount, maxSerial) + request.getExtraCount();
        pkg.setUnitCount(newTotalUnitCount);
        packageRepository.merge(pkg);

        auditLogRepository.persist(buildAuditLog(
                actorId,
                "QR_EXTRA_GENERATED",
                "packages",
                pkg.getId(),
                toJson(Map.of(
                        "batchId", batchId,
                        "previousUnitCount", previousUnitCount,
                        "addedCount", request.getExtraCount(),
                        "totalUnitCount", newTotalUnitCount,
                        "fromSerial", fromSerial,
                        "toSerial", toSerial,
                        "reason", request.getReason().trim()
                )),
                ipAddr
        ));

        ExtraQrResponse resp = ExtraQrResponse.builder()
                .batchId(batchId)
                .packageId(pkg.getId())
                .previousUnitCount(previousUnitCount)
                .addedCount(request.getExtraCount())
                .totalUnitCount(newTotalUnitCount)
                .fromSerial(fromSerial)
                .toSerial(toSerial)
                .reason(request.getReason().trim())
                .codes(extraCodes.stream().map(QrCode::getCode).toList())
                .build();

        return BaseResponse.build(resp);
    }

    private List<String> validateActivationPreconditions(Batch batch, List<CultivationEvent> events) {
        List<String> missing = new ArrayList<>();

        if (!"QC_PASSED".equalsIgnoreCase(batch.getStatus())) {
            missing.add("Batch status must be QC_PASSED (current: " + batch.getStatus() + ")");
        }

        boolean hasHarvestEvent = events.stream().anyMatch(e ->
                ("THU_HOACH".equalsIgnoreCase(e.getEventType()) || "HARVESTING".equalsIgnoreCase(e.getEventType()))
                        && !"REJECTED".equalsIgnoreCase(e.getStatus()));
        if (batch.getHarvestDate() == null && !hasHarvestEvent) {
            missing.add("Batch has no harvest record (missing harvest_date or harvest event)");
        }

        if (events.isEmpty()) {
            missing.add("Batch has no cultivation events");
        } else {
            for (CultivationEvent event : events) {
                String status = event.getStatus();
                if (!"APPROVED".equalsIgnoreCase(status) && !"SUPERSEDED".equalsIgnoreCase(status)) {
                    missing.add("Event seq=" + event.getSeq() + " (" + event.getEventType()
                            + ") has status '" + status + "', must be CONFIRMED & APPROVED or SUPERSEDED");
                }
            }
        }

        return missing;
    }

    private List<QrCode> generateSerialBatch(PackageEntity pkg, int fromSerial, int toSerial, String status, long now) {
        List<QrCode> created = new ArrayList<>();
        for (int serial = fromSerial; serial <= toSerial; serial++) {
            String code;
            do {
                code = qrLabelExportService.generateRandomCode();
            } while (qrCodeRepository.existsByCode(code));

            QrCode qr = new QrCode();
            qr.setPkg(pkg);
            qr.setCode(code);
            qr.setSerial(serial);
            qr.setStatus(status);
            qr.setPrintedAt("PRINTED".equals(status) ? now : null);
            qr.setScanCount(0);
            qr.setReprintCount(0);
            qrCodeRepository.persist(qr);
            created.add(qr);
        }
        return created;
    }

    private AuditLog buildAuditLog(Long actorId, String action, String entity, Long entityId, String detail, String ipAddr) {
        return AuditLog.builder()
                .actor(userRepository.findById(actorId).orElse(null))
                .action(action)
                .entity(entity)
                .entityId(entityId)
                .detail(detail)
                .ipAddr(ipAddr)
                .build();
    }

    private List<QrCode> filterByRange(List<QrCode> list, Integer fromSerial, Integer toSerial) {
        if (fromSerial == null && toSerial == null) return list;
        return list.stream()
                .filter(q -> (fromSerial == null || q.getSerial() >= fromSerial)
                        && (toSerial == null || q.getSerial() <= toSerial))
                .toList();
    }

    private String normalizeFormat(String format) {
        if (format == null || format.isBlank()) return "pdf";
        String f = format.trim().toLowerCase();
        return switch (f) {
            case "zpl", "csv" -> f;
            default -> "pdf";
        };
    }

    private String buildFinalizePayload(Batch batch, MerkleSnapshot snapshot, PackageEntity pkg, String merkleRoot) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("action", "FINALIZE_BATCH");
        payload.put("batchId", batch.getId());
        payload.put("batchCode", batch.getBatchCode());
        payload.put("snapshotId", snapshot.getId());
        payload.put("merkleRoot", merkleRoot);
        payload.put("leafCount", snapshot.getLeafCount());
        payload.put("packageId", pkg.getId());
        payload.put("unitCount", pkg.getUnitCount());
        return toJson(payload);
    }

    private String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }
}
