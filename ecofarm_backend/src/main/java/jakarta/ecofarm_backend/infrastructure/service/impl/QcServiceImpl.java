package jakarta.ecofarm_backend.infrastructure.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ecofarm_backend.common.dto.request.HarvestBatchParam;
import jakarta.ecofarm_backend.common.dto.request.HarvestRequest;
import jakarta.ecofarm_backend.common.dto.request.RecordQcParam;
import jakarta.ecofarm_backend.common.dto.request.RecordQcRequest;
import jakarta.ecofarm_backend.common.dto.request.ReviewQcParam;
import jakarta.ecofarm_backend.common.dto.request.ReviewQcRequest;
import jakarta.ecofarm_backend.common.dto.response.HarvestResponse;
import jakarta.ecofarm_backend.common.dto.response.QcResponse;
import jakarta.ecofarm_backend.common.enums.*;
import jakarta.ecofarm_backend.common.exception.ConflictException;
import jakarta.ecofarm_backend.common.exception.EntityNotFoundException;
import jakarta.ecofarm_backend.common.exception.PermissionDeniedException;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.*;
import jakarta.ecofarm_backend.infrastructure.service.*;
import jakarta.ecofarm_backend.modules.blockchain.client.BesuClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.web3j.crypto.Hash;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@ApplicationScoped
public class QcServiceImpl implements QcService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Inject
    private BatchService batchService;

    @Inject
    private QcRecordService qcRecordService;

    @Inject
    private CultivationEventService cultivationEventService;

    @Inject
    private OutboxEventService outboxEventService;

    @Inject
    private AuditLogService auditLogService;

    @Inject
    private UserService userService;

    @Inject
    private BesuClient besuClient;

    @Inject
    private AnchorService anchorService;

    @lombok.Builder
    private record EvaluateQcParam(
            Batch batch,
            List<CultivationEvent> events,
            RecordQcRequest request,
            int currentAttempt,
            double avgBrix,
            double avgWeight
    ) {}

    private record QcAssessment(
            boolean passed,
            String status,
            String failureReason,
            Integer deficitDays,
            Long actualIsolationDays,
            Integer requiredIsolationDays,
            String message,
            Boolean systemSuggestedPassed,
            LocalDate expectedHarvestDate,
            Long daysSinceSowing,
            Map<String, Object> criteriaEvaluation
    ) {}

    @Override
    @Transactional
    public QcResponse recordQc(RecordQcParam param) {
        RecordQcRequest request = param.getRequest();
        Batch batch = Optional.ofNullable(batchService.findById(param.getBatchId()))
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND));

        User actor = userService.findById(param.getActorId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        validateEngineerPermission(batch, actor);
        BatchStatus currentStatus = Optional.ofNullable(batch.getStatus())
                .map(BatchStatus::valueOf)
                .orElse(BatchStatus.DRAFT);

        if (currentStatus == BatchStatus.REJECTED || currentStatus.isLocked()) {
            throw new ConflictException(ErrorCode.QC_BATCH_STATUS_INVALID,
                    List.of("Lô đang ở trạng thái " + currentStatus + ", không thể thực hiện kiểm định chất lượng"));
        }
        Optional<QcRecord> existingRecord = Optional.ofNullable(
                qcRecordService.findByClientEventId(request.getClientEventId()));
        if (existingRecord.isPresent()) {
            return toResponse(existingRecord.get(), "Bản ghi kiểm định QC đã được xử lý trước đó");
        }

        if (request.getBrixSamples() == null || request.getBrixSamples().size() != 5 ||
                request.getWeightSamples() == null || request.getWeightSamples().size() != 5) {
            throw new ConflictException(ErrorCode.QC_SAMPLE_COUNT_INVALID);
        }

        double sumBrix = request.getBrixSamples().stream().mapToDouble(Double::doubleValue).sum();
        double avgBrix = Math.round((sumBrix / 5.0) * 10.0) / 10.0;

        double sumWeight = request.getWeightSamples().stream().mapToDouble(Double::doubleValue).sum();
        double avgWeight = Math.round((sumWeight / 5.0) * 100.0) / 100.0;

        int previousAttempts = qcRecordService.countByBatchId(param.getBatchId());
        int currentAttempt = previousAttempts + 1;
        if (currentAttempt > 3) {
            throw new ConflictException(ErrorCode.QC_MAX_ATTEMPTS_EXCEEDED);
        }

        List<CultivationEvent> events = cultivationEventService.findByBatchIdOrderBySeqAsc(param.getBatchId());
        EvaluateQcParam evalParam = EvaluateQcParam.builder()
                .batch(batch)
                .events(events)
                .request(request)
                .currentAttempt(currentAttempt)
                .avgBrix(avgBrix)
                .avgWeight(avgWeight)
                .build();
        QcAssessment assessment = evaluateQc(evalParam);

        if ("REJECTED".equals(assessment.status())) {
            batch.setStatus(BatchStatus.REJECTED.name());
        } else if ("QC_PASSED".equals(assessment.status())) {
            batch.setStatus(BatchStatus.QC_PASSED.name());
        }

        supersedePreviousQcRecordsAndEvents(events, param.getBatchId());

        long now = System.currentTimeMillis();
        QcRecord newQcRecord = QcRecord.builder()
                .batch(batch)
                .brix(avgBrix)
                .avgWeightKg(avgWeight)
                .passed(assessment.passed())
                .inspectedByUser(actor)
                .inspectedAt(now)
                .attemptNumber(currentAttempt)
                .failureReason(assessment.failureReason())
                .deficitDays(assessment.deficitDays())
                .sampleBrix(toJson(request.getBrixSamples()))
                .sampleWeights(toJson(request.getWeightSamples()))
                .status(assessment.status())
                .clientEventId(request.getClientEventId())
                .photoHashes(Optional.ofNullable(request.getPhotoHashes()).map(QcServiceImpl::toJson).orElse(null))
                .systemSuggestedPassed(assessment.systemSuggestedPassed())
                .criteriaEvaluation(toJson(assessment.criteriaEvaluation()))
                .build();
        qcRecordService.persist(newQcRecord);

        String actorRef = "usr_" + param.getActorId();
        int nextSeq = cultivationEventService.findNextSeqByBatchId(param.getBatchId());

        Map<String, Object> payloadMap = buildQcPayload(evalParam, assessment, actorRef, now);
        String eventPayloadJson = toJson(payloadMap);
        String eventHash = Hash.sha3String(eventPayloadJson);

        EventChainStatus chainStatus = "PENDING_REVIEW".equals(assessment.status()) ? EventChainStatus.PENDING : EventChainStatus.CONFIRMED;
        EventReviewStatus reviewStatus = "PENDING_REVIEW".equals(assessment.status()) ? EventReviewStatus.UNREVIEWED : EventReviewStatus.APPROVED;

        CultivationEvent cultivationEvent = CultivationEvent.builder()
                .batch(batch)
                .seq(nextSeq)
                .eventType(EventType.QC)
                .payload(eventPayloadJson)
                .eventHash(eventHash)
                .status(chainStatus)
                .reviewStatus(reviewStatus)
                .occurredAt(Optional.ofNullable(request.getOccurredAt()).orElse(now))
                .recordedBy(actor)
                .recordedAt(now)
                .clientEventId(request.getClientEventId())
                .build();
        cultivationEventService.persist(cultivationEvent);

        String batchKey = Optional.ofNullable(batch.getBatchCode()).orElse(String.valueOf(param.getBatchId()));
        String batchIdHash = Hash.sha3String(batchKey);
        int outboxSeq = outboxEventService.findNextSeqByBatchIdHash(batchIdHash);

        OutboxEvent outbox = OutboxEvent.builder()
                .batchIdHash(batchIdHash)
                .seq(outboxSeq)
                .eventData(eventPayloadJson)
                .status(OutboxEvent.OutboxStatus.PENDING)
                .attempts(0)
                .build();
        outboxEventService.persist(outbox);

        batchService.merge(batch);

        AuditLog auditLog = AuditLog.builder()
                .actor(actor)
                .action("QC_RECORD")
                .entity("batches")
                .entityId(batch.getId())
                .detail(toJson(Map.of(
                        "qcRecordId", newQcRecord.getId(),
                        "status", assessment.status(),
                        "passed", assessment.passed(),
                        "failureReason", Optional.ofNullable(assessment.failureReason()).orElse("NONE"),
                        "attemptNumber", currentAttempt,
                        "avgBrix", avgBrix,
                        "avgWeightKg", avgWeight
                )))
                .ipAddr(param.getClientIp())
                .build();
        auditLogService.persist(auditLog);

        return toResponse(newQcRecord, assessment.message());
    }

    @Override
    @Transactional
    public QcResponse reviewQc(ReviewQcParam param) {
        ReviewQcRequest request = param.getRequest();

        User admin = userService.findById(param.getAdminId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        if (admin.getRoles().stream().noneMatch(r -> r.getRoleName() == RoleEnum.ADMIN)) {
            throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
        }
        String adminNote = Optional.ofNullable(request.getNote())
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElseThrow(() -> new ConflictException(ErrorCode.QC_ADMIN_NOTE_REQUIRED));

        Batch batch = Optional.ofNullable(batchService.findById(param.getBatchId()))
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND));

        QcRecord pendingQc = Optional.ofNullable(qcRecordService.findLatestByBatchId(param.getBatchId()))
                .filter(q -> "PENDING_REVIEW".equalsIgnoreCase(q.getStatus()))
                .orElseThrow(() -> new ConflictException(ErrorCode.QC_RECORD_NOT_FOUND,
                        List.of("Không có lần kiểm định QC nào của lô đang ở trạng thái chờ kiểm duyệt")));

        long now = System.currentTimeMillis();
        boolean approved = "APPROVED".equalsIgnoreCase(request.getDecision());

        if (approved) {
            boolean engineerPassed = Boolean.TRUE.equals(pendingQc.getPassed());
            if (engineerPassed) {
                pendingQc.setStatus("QC_PASSED");
                pendingQc.setFailureReason(null);
                batch.setStatus(BatchStatus.QC_PASSED.name());
            } else {
                boolean maxAttempts = Optional.ofNullable(pendingQc.getAttemptNumber()).orElse(1) >= 3;
                pendingQc.setStatus(maxAttempts ? "REJECTED" : "FAILED");
                pendingQc.setFailureReason("QUALITY_NOT_MET");
                if (maxAttempts) {
                    batch.setStatus(BatchStatus.REJECTED.name());
                }
            }
        } else {
            pendingQc.setStatus("REJECTED");
            pendingQc.setPassed(false);
            pendingQc.setFailureReason("ADMIN_REJECTED: " + adminNote);
            batch.setStatus(BatchStatus.REJECTED.name());
        }

        pendingQc.setAdminDecision(request.getDecision().toUpperCase());
        pendingQc.setAdminNote(adminNote);
        pendingQc.setReviewedByUser(admin);
        pendingQc.setReviewedAt(now);

        qcRecordService.merge(pendingQc);
        batchService.merge(batch);

        Optional.ofNullable(pendingQc.getClientEventId())
                .map(cultivationEventService::findByClientEventId)
                .ifPresent(event -> {
                    event.setReviewStatus(approved ? EventReviewStatus.APPROVED : EventReviewStatus.REJECTED);
                    event.setStatus(approved ? EventChainStatus.CONFIRMED : EventChainStatus.SUPERSEDED);
                    event.setReviewedBy(admin);
                    event.setReviewedAt(now);
                    if (!approved) {
                        event.setRejectReason(adminNote);
                    }
                    cultivationEventService.merge(event);
                });

        AuditLog auditLog = AuditLog.builder()
                .actor(admin)
                .action("QC_ADMIN_REVIEW")
                .entity("batches")
                .entityId(batch.getId())
                .detail(toJson(Map.of(
                        "qcRecordId", pendingQc.getId(),
                        "decision", request.getDecision(),
                        "note", adminNote,
                        "finalStatus", pendingQc.getStatus()
                )))
                .ipAddr(param.getClientIp())
                .build();
        auditLogService.persist(auditLog);

        String msg = approved ? "Admin đã duyệt kết quả kiểm định QC" : "Admin đã từ chối kiểm định QC; lô đã bị hủy";
        return toResponse(pendingQc, msg);
    }

    @Override
    public QcResponse getLatestQc(Long batchId) {
        Optional.ofNullable(batchService.findById(batchId))
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND));

        return Optional.ofNullable(qcRecordService.findLatestByBatchId(batchId))
                .map(r -> toResponse(r, "Lấy thông tin QC mới nhất thành công"))
                .orElse(null);
    }

    @Override
    @Transactional
    public HarvestResponse harvestBatch(HarvestBatchParam param) {
        HarvestRequest request = param.getRequest();

        Batch batch = Optional.ofNullable(batchService.findById(param.getBatchId()))
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND));

        User actor = userService.findById(param.getActorId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        validateHarvestPermission(batch, actor);

        if (!BatchStatus.QC_PASSED.name().equalsIgnoreCase(batch.getStatus())) {
            throw new ConflictException(ErrorCode.PRE_HARVEST_QC_REQUIRED,
                    List.of("Lô " + batch.getBatchCode() + " chưa đạt kiểm định chất lượng trước thu hoạch (trạng thái: "
                            + batch.getStatus() + "). Chỉ cho phép thu hoạch khi lô ở trạng thái QC_PASSED."));
        }

        Optional<CultivationEvent> existingHarvest = Optional.ofNullable(
                cultivationEventService.findByClientEventId(request.getClientEventId()));
        if (existingHarvest.isPresent()) {
            return HarvestResponse.builder()
                    .batchId(param.getBatchId())
                    .batchCode(batch.getBatchCode())
                    .status(batch.getStatus())
                    .harvestDate(Optional.ofNullable(batch.getHarvestDate()).map(LocalDate::toString).orElse(LocalDate.now().toString()))
                    .estimatedQuantity(request.getEstimatedQuantity())
                    .area(request.getArea())
                    .eventHash(existingHarvest.get().getEventHash())
                    .message("Sự kiện thu hoạch đã được ghi nhận trước đó")
                    .build();
        }

        LocalDate harvestDate = LocalDate.now();
        batch.setHarvestDate(harvestDate);
        batch.setStatus(BatchStatus.HARVESTED.name());
        batchService.merge(batch);

        long now = System.currentTimeMillis();
        long occurredAt = Optional.ofNullable(request.getHarvestedAt()).orElse(now);
        String actorRef = "usr_" + param.getActorId();
        int nextSeq = cultivationEventService.findNextSeqByBatchId(param.getBatchId());

        Map<String, Object> payloadMap = Map.of(
                "schemaVersion", "1.0",
                "eventType", "HARVEST",
                "batchCode", Optional.ofNullable(batch.getBatchCode()).orElse(""),
                "occurredAt", occurredAt,
                "actorRef", actorRef,
                "data", Map.of(
                        "estimatedQuantity", Optional.ofNullable(request.getEstimatedQuantity()).orElse(0),
                        "area", Optional.ofNullable(request.getArea()).orElse(""),
                        "harvestDate", harvestDate.toString(),
                        "note", Optional.ofNullable(request.getNote()).orElse("")
                )
        );

        String payloadJson = toJson(payloadMap);
        String eventHash = Hash.sha3String(payloadJson);

        CultivationEvent harvestEvent = CultivationEvent.builder()
                .batch(batch)
                .seq(nextSeq)
                .eventType(EventType.HARVEST)
                .payload(payloadJson)
                .eventHash(eventHash)
                .status(EventChainStatus.CONFIRMED)
                .reviewStatus(EventReviewStatus.APPROVED)
                .occurredAt(occurredAt)
                .recordedBy(actor)
                .recordedAt(now)
                .clientEventId(request.getClientEventId())
                .build();
        cultivationEventService.persist(harvestEvent);

        String besuTxHash = null;
        Long besuBlockNumber = null;
        String explorerUrl = null;

        try {
            besuTxHash = besuClient.sendTransaction(payloadJson);
            besuBlockNumber = besuClient.getCurrentBlockNumber();
            explorerUrl = "http://localhost:8545/tx/" + besuTxHash;

            Anchor anchor = Anchor.builder()
                    .txHash(besuTxHash)
                    .status(AnchorStatus.SUBMITTED.name())
                    .blockNumber(besuBlockNumber)
                    .confirmations(0)
                    .explorerUrl(explorerUrl)
                    .anchoredAt(now)
                    .build();
            anchorService.persist(anchor);
        } catch (Exception besuEx) {
            System.err.println("Besu anchoring error during harvest: " + besuEx.getMessage());
        }
        String batchKey = Optional.ofNullable(batch.getBatchCode()).orElse(String.valueOf(param.getBatchId()));
        String batchIdHash = Hash.sha3String(batchKey);
        int outboxSeq = outboxEventService.findNextSeqByBatchIdHash(batchIdHash);

        OutboxEvent outbox = OutboxEvent.builder()
                .batchIdHash(batchIdHash)
                .seq(outboxSeq)
                .eventData(payloadJson)
                .status(besuTxHash != null ? OutboxEvent.OutboxStatus.SUCCESS : OutboxEvent.OutboxStatus.PENDING)
                .attempts(besuTxHash != null ? 1 : 0)
                .build();
        outboxEventService.persist(outbox);

        AuditLog auditLog = AuditLog.builder()
                .actor(actor)
                .action("BATCH_HARVEST")
                .entity("batches")
                .entityId(batch.getId())
                .detail(toJson(Map.of(
                        "harvestDate", harvestDate.toString(),
                        "estimatedQuantity", Optional.ofNullable(request.getEstimatedQuantity()).orElse(0),
                        "besuTxHash", Optional.ofNullable(besuTxHash).orElse("PENDING_OUTBOX"),
                        "status", BatchStatus.HARVESTED.name()
                )))
                .ipAddr(param.getClientIp())
                .build();
        auditLogService.persist(auditLog);

        return HarvestResponse.builder()
                .batchId(param.getBatchId())
                .batchCode(batch.getBatchCode())
                .status(BatchStatus.HARVESTED.name())
                .harvestDate(harvestDate.toString())
                .estimatedQuantity(request.getEstimatedQuantity())
                .area(request.getArea())
                .eventHash(eventHash)
                .besuTxHash(besuTxHash)
                .besuBlockNumber(besuBlockNumber)
                .explorerUrl(explorerUrl)
                .message("Thu hoạch lô thành công và đã kích hoạt neo dữ liệu lên Besu Private Blockchain")
                .build();
    }

    private QcAssessment evaluateQc(EvaluateQcParam evalParam) {
        Batch batch = evalParam.batch();
        List<CultivationEvent> events = evalParam.events();
        RecordQcRequest request = evalParam.request();
        int currentAttempt = evalParam.currentAttempt();
        double avgBrix = evalParam.avgBrix();
        double avgWeight = evalParam.avgWeight();

        int minIsolationDays = Math.max(15, Optional.ofNullable(batch.getVariety())
                .map(Variety::getMinIsolationDays)
                .orElse(15));

        LocalDate sowDate = Optional.ofNullable(batch.getSowDate())
                .orElseGet(() -> events.stream()
                        .filter(e -> e.getEventType() == EventType.SOWING)
                        .findFirst()
                        .map(e -> Optional.ofNullable(e.getOccurredAt()).orElse(e.getCreatedAt()))
                        .map(epoch -> LocalDate.ofInstant(Instant.ofEpochMilli(epoch), ZoneId.systemDefault()))
                        .orElse(null));

        LocalDate expectedHarvestDate = sowDate != null ? sowDate.plusDays(80) : null;
        Long daysSinceSowing = sowDate != null ? ChronoUnit.DAYS.between(sowDate, LocalDate.now()) : null;

        boolean brixPass = avgBrix >= 13.0 && avgBrix <= 15.0;
        boolean weightPass = avgWeight >= 1.2 && avgWeight <= 1.8;

        Optional<CultivationEvent> isolationEventOpt = events.stream()
                .filter(e -> e.getEventType() == EventType.POLLINATION_ISOLATION)
                .reduce((first, second) -> second);

        if (isolationEventOpt.isEmpty()) {
            Map<String, Object> criteriaEval = buildCriteriaEvaluation(avgBrix, brixPass, avgWeight, weightPass,
                    minIsolationDays, null, null, false,
                    sowDate, expectedHarvestDate, daysSinceSowing, false, "ENGINEER_PENDING_ADMIN");

            return new QcAssessment(
                    Boolean.TRUE.equals(request.getPassed()),
                    "PENDING_REVIEW",
                    "WAITING_ADMIN_ISOLATION_APPROVAL",
                    null,
                    null,
                    minIsolationDays,
                    "Chưa có sự kiện ngừng phân cách ly; kết quả QC đang chờ Admin kiểm duyệt",
                    false,
                    expectedHarvestDate,
                    daysSinceSowing,
                    criteriaEval
            );
        }

        CultivationEvent isolationEvent = isolationEventOpt.get();
        Long isolationEpoch = Optional.ofNullable(isolationEvent.getOccurredAt()).orElse(isolationEvent.getCreatedAt());
        LocalDate isolationDate = LocalDate.ofInstant(Instant.ofEpochMilli(isolationEpoch), ZoneId.systemDefault());
        LocalDate harvestDate = resolveHarvestDate(request.getHarvestDate(), events, batch);

        long actualIsolationDays = Math.max(0, ChronoUnit.DAYS.between(isolationDate, harvestDate));
        int deficit = (int) Math.max(0, minIsolationDays - actualIsolationDays);
        boolean quarantinePass = deficit == 0;

        boolean systemSuggestedPassed = brixPass && weightPass && quarantinePass;

        Map<String, Object> criteriaEval = buildCriteriaEvaluation(avgBrix, brixPass, avgWeight, weightPass,
                minIsolationDays, actualIsolationDays, deficit, quarantinePass,
                sowDate, expectedHarvestDate, daysSinceSowing, systemSuggestedPassed, "ENGINEER");

        if (deficit > 0) {
            return new QcAssessment(
                    false,
                    "REJECTED",
                    "QUARANTINE_NOT_MET:DEFICIT_" + deficit,
                    deficit,
                    actualIsolationDays,
                    minIsolationDays,
                    "Thời gian cách ly chưa đủ (" + deficit + " ngày còn thiếu so với quy định tối thiểu " + minIsolationDays + " ngày). Lô bị hủy và không được thu hoạch.",
                    false,
                    expectedHarvestDate,
                    daysSinceSowing,
                    criteriaEval
            );
        }
        if (!Boolean.TRUE.equals(request.getPassed())) {
            boolean maxExceeded = currentAttempt >= 3;
            return new QcAssessment(
                    false,
                    maxExceeded ? "REJECTED" : "FAILED",
                    "QUALITY_NOT_MET",
                    0,
                    actualIsolationDays,
                    minIsolationDays,
                    maxExceeded ? "Đã hết 3 lần QC vẫn không đạt chất lượng; lô bị hủy."
                            : "Kỹ sư đánh giá không đạt chất lượng (Lần " + currentAttempt + "/3). Cho phép QC lại.",
                    systemSuggestedPassed,
                    expectedHarvestDate,
                    daysSinceSowing,
                    criteriaEval
            );
        }

        return new QcAssessment(
                true,
                "QC_PASSED",
                null,
                0,
                actualIsolationDays,
                minIsolationDays,
                "QC trước thu hoạch ĐẠT (Gợi ý hệ thống: " + (systemSuggestedPassed ? "Đạt" : "Cần xem xét") + ", Kỹ sư quyết định: Đạt). Lô chuyển sang trạng thái QC_PASSED và ĐƯỢC PHÉP THU HOẠCH.",
                systemSuggestedPassed,
                expectedHarvestDate,
                daysSinceSowing,
                criteriaEval
        );
    }

    private Map<String, Object> buildCriteriaEvaluation(double avgBrix, boolean brixPass,
                                                       double avgWeight, boolean weightPass,
                                                       int requiredQuarantineDays, Long actualQuarantineDays,
                                                       Integer deficitDays, boolean quarantinePass,
                                                       LocalDate sowDate, LocalDate expectedHarvestDate,
                                                       Long daysSinceSowing, boolean systemSuggestedPassed,
                                                       String decisionRole) {
        Map<String, Object> criteria = new LinkedHashMap<>();
        criteria.put("brix", Map.of(
                "avgBrix", avgBrix,
                "standardRange", "13.0 - 15.0 °Bx",
                "passed", brixPass
        ));
        criteria.put("weight", Map.of(
                "avgWeightKg", avgWeight,
                "standardRange", "1.2 - 1.8 kg",
                "passed", weightPass
        ));
        criteria.put("quarantine", Map.of(
                "requiredDays", requiredQuarantineDays,
                "actualDays", actualQuarantineDays != null ? String.valueOf(actualQuarantineDays) : "CHƯA_CÓ_SỰ_KIỆN_NGỪNG_PHÂN",
                "deficitDays", deficitDays != null ? deficitDays : 0,
                "passed", quarantinePass
        ));
        criteria.put("harvestProjection", Map.of(
                "sowDate", sowDate != null ? sowDate.toString() : "N/A",
                "expectedHarvestDate", expectedHarvestDate != null ? expectedHarvestDate.toString() : "N/A",
                "daysSinceSowing", daysSinceSowing != null ? daysSinceSowing : 0,
                "standardGrowthDays", 80
        ));
        criteria.put("systemSuggestedPassed", systemSuggestedPassed);
        criteria.put("finalDecisionBy", decisionRole);
        return criteria;
    }

    private void validateEngineerPermission(Batch batch, User actor) {
        boolean isAdmin = actor.getRoles().stream().anyMatch(r -> r.getRoleName() == RoleEnum.ADMIN);
        boolean isEngineer = actor.getRoles().stream().anyMatch(r -> r.getRoleName() == RoleEnum.ENGINEER);

        if (!isAdmin && !isEngineer) {
            throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
        }

        if (isAdmin) {
            return;
        }

        boolean isAssigned = batchService.isAssigned(batch.getId(), actor.getId());
        boolean isCreator = Optional.ofNullable(batch.getCreatedBy())
                .map(User::getId)
                .map(id -> id.equals(actor.getId()))
                .orElse(false);

        if (!isAssigned && !isCreator) {
            throw new PermissionDeniedException(ErrorCode.BATCH_NOT_ASSIGNED);
        }
    }

    private void validateHarvestPermission(Batch batch, User actor) {
        boolean isAdmin = actor.getRoles().stream().anyMatch(r -> r.getRoleName() == RoleEnum.ADMIN);
        boolean isEngineer = actor.getRoles().stream().anyMatch(r -> r.getRoleName() == RoleEnum.ENGINEER);
        boolean isWorker = actor.getRoles().stream().anyMatch(r -> r.getRoleName() == RoleEnum.FARMER);

        if (!isAdmin && !isEngineer && !isWorker) {
            throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
        }

        if (isAdmin) {
            return;
        }

        boolean isAssigned = batchService.isAssigned(batch.getId(), actor.getId());
        boolean isCreator = Optional.ofNullable(batch.getCreatedBy())
                .map(User::getId)
                .map(id -> id.equals(actor.getId()))
                .orElse(false);

        if (!isAssigned && !isCreator) {
            throw new PermissionDeniedException(ErrorCode.BATCH_NOT_ASSIGNED);
        }
    }

    private void supersedePreviousQcRecordsAndEvents(List<CultivationEvent> events, Long batchId) {
        events.stream()
                .filter(ev -> ev.getEventType() == EventType.QC
                        && (ev.getStatus() == EventChainStatus.CONFIRMED || ev.getStatus() == EventChainStatus.PENDING))
                .forEach(ev -> {
                    ev.setStatus(EventChainStatus.SUPERSEDED);
                    cultivationEventService.merge(ev);
                });

        qcRecordService.findByBatchId(batchId).stream()
                .filter(qr -> !"SUPERSEDED".equalsIgnoreCase(qr.getStatus()))
                .forEach(qr -> {
                    qr.setStatus("SUPERSEDED");
                    qcRecordService.merge(qr);
                });
    }

    private Map<String, Object> buildQcPayload(EvaluateQcParam evalParam, QcAssessment assessment,
                                               String actorRef, long now) {
        Map<String, Object> payloadMap = new LinkedHashMap<>();
        payloadMap.put("schemaVersion", "1.0");
        payloadMap.put("eventType", "QC");
        payloadMap.put("batchCode", Optional.ofNullable(evalParam.batch().getBatchCode()).orElse(""));
        payloadMap.put("occurredAt", Optional.ofNullable(evalParam.request().getOccurredAt()).orElse(now));
        payloadMap.put("actorRef", actorRef);

        Map<String, Object> dataMap = new LinkedHashMap<>();
        dataMap.put("brixSamples", evalParam.request().getBrixSamples());
        dataMap.put("weightSamples", evalParam.request().getWeightSamples());
        dataMap.put("avgBrix", evalParam.avgBrix());
        dataMap.put("avgWeightKg", evalParam.avgWeight());
        dataMap.put("passed", assessment.passed());
        dataMap.put("status", assessment.status());
        dataMap.put("failureReason", assessment.failureReason());
        dataMap.put("attemptNumber", evalParam.currentAttempt());
        dataMap.put("quarantineDaysActual", assessment.actualIsolationDays());
        dataMap.put("quarantineDaysRequired", assessment.requiredIsolationDays());
        dataMap.put("quarantineDaysDeficit", assessment.deficitDays());
        dataMap.put("systemSuggestedPassed", assessment.systemSuggestedPassed());
        dataMap.put("expectedHarvestDate", Optional.ofNullable(assessment.expectedHarvestDate()).map(LocalDate::toString).orElse(null));
        dataMap.put("daysSinceSowing", assessment.daysSinceSowing());
        dataMap.put("criteriaEvaluation", assessment.criteriaEvaluation());
        dataMap.put("note", Optional.ofNullable(evalParam.request().getNote()).orElse(""));
        payloadMap.put("data", dataMap);
        payloadMap.put("attachments", Optional.ofNullable(evalParam.request().getPhotoHashes()).orElse(List.of()));

        return payloadMap;
    }

    private LocalDate resolveHarvestDate(String inputDate, List<CultivationEvent> events, Batch batch) {
        if (inputDate != null && !inputDate.isBlank()) {
            try {
                return LocalDate.parse(inputDate);
            } catch (Exception ignored) {
            }
        }

        return events.stream()
                .filter(e -> e.getEventType() == EventType.HARVEST && e.getOccurredAt() != null)
                .findFirst()
                .map(e -> LocalDate.ofInstant(Instant.ofEpochMilli(e.getOccurredAt()), ZoneId.systemDefault()))
                .orElseGet(() -> Optional.ofNullable(batch.getHarvestDate()).orElse(LocalDate.now()));
    }

    private QcResponse toResponse(QcRecord record, String message) {
        List<Double> brixSamples = fromJson(record.getSampleBrix(), new TypeReference<List<Double>>() {});
        List<Double> weightSamples = fromJson(record.getSampleWeights(), new TypeReference<List<Double>>() {});
        List<String> photoHashes = fromJson(record.getPhotoHashes(), new TypeReference<List<String>>() {});
        Map<String, Object> criteriaEval = fromJson(record.getCriteriaEvaluation(), new TypeReference<Map<String, Object>>() {});

        Long batchId = Optional.ofNullable(record.getBatch()).map(Batch::getId).orElse(null);
        String batchCode = Optional.ofNullable(record.getBatch()).map(Batch::getBatchCode).orElse(null);
        Integer requiredDays = Optional.ofNullable(record.getBatch())
                .map(Batch::getVariety)
                .map(Variety::getMinIsolationDays)
                .orElse(15);

        LocalDate sowDate = Optional.ofNullable(record.getBatch()).map(Batch::getSowDate).orElse(null);
        String expectedHarvestDate = sowDate != null ? sowDate.plusDays(80).toString() : null;
        Long daysSinceSowing = sowDate != null ? ChronoUnit.DAYS.between(sowDate, LocalDate.now()) : null;

        Long inspectedById = Optional.ofNullable(record.getInspectedByUser()).map(User::getId).orElse(null);
        Long reviewedById = Optional.ofNullable(record.getReviewedByUser()).map(User::getId).orElse(null);

        String actorRef = inspectedById != null ? "usr_" + inspectedById : null;

        return QcResponse.builder()
                .id(record.getId())
                .batchId(batchId)
                .batchCode(batchCode)
                .avgBrix(record.getBrix())
                .avgWeightKg(record.getAvgWeightKg())
                .passed(record.getPassed())
                .status(record.getStatus())
                .attemptNumber(record.getAttemptNumber())
                .failureReason(record.getFailureReason())
                .quarantineDaysRequired(requiredDays)
                .quarantineDaysDeficit(record.getDeficitDays())
                .brixSamples(brixSamples)
                .weightSamples(weightSamples)
                .photoHashes(photoHashes)
                .inspectedBy(inspectedById)
                .inspectedAt(record.getInspectedAt())
                .actorRef(actorRef)
                .clientEventId(record.getClientEventId())
                .adminDecision(record.getAdminDecision())
                .adminNote(record.getAdminNote())
                .reviewedBy(reviewedById)
                .reviewedAt(record.getReviewedAt())
                .systemSuggestedPassed(record.getSystemSuggestedPassed())
                .expectedHarvestDate(expectedHarvestDate)
                .daysSinceSowing(daysSinceSowing)
                .criteriaEvaluation(criteriaEval)
                .message(message)
                .build();
    }

    private static String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi chuyển đổi dữ liệu sang JSON: " + e.getMessage(), e);
        }
    }

    private static <T> T fromJson(String json, TypeReference<T> ref) {
        if (json == null || json.isBlank()) return null;
        try {
            return MAPPER.readValue(json, ref);
        } catch (Exception e) {
            return null;
        }
    }
}
