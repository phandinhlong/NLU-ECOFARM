package jakarta.ecofarm_backend.modules.quality.facade.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.BatchResponse;
import jakarta.ecofarm_backend.common.dto.response.QcResponse;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.exception.BadRequestException;
import jakarta.ecofarm_backend.common.exception.ConflictException;
import jakarta.ecofarm_backend.common.exception.EntityNotFoundException;
import jakarta.ecofarm_backend.common.exception.PermissionDeniedException;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.QcRecord;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Variety;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.BatchRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.CultivationEventRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.OutboxEventRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.QcRecordRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.UserRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.VarietyRepository;
import jakarta.ecofarm_backend.modules.log.EventHasher;
import jakarta.ecofarm_backend.modules.log.EventStatus;
import jakarta.ecofarm_backend.modules.log.EventType;
import jakarta.ecofarm_backend.modules.log.EventWriteGuard;
import jakarta.ecofarm_backend.modules.quality.IsolationInputs;
import jakarta.ecofarm_backend.modules.quality.IsolationPolicy;
import jakarta.ecofarm_backend.modules.quality.QcStaleness;
import jakarta.ecofarm_backend.modules.quality.QualityRequests.HarvestRequest;
import jakarta.ecofarm_backend.modules.quality.QualityRequests.QcRequest;
import jakarta.ecofarm_backend.modules.quality.facade.QualityFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

/**
 * Logic nghiệp vụ cho thu hoạch và QC. Cả hai thao tác ghi đều khóa dòng batch (qua repository) và
 * chạy trong một transaction. Thu hoạch ghi một sự kiện nhật ký hệ thống {@code THU_HOACH} (seq,
 * hash, outbox riêng) rồi chuyển batch sang HARVESTED. QC tự tính lại đạt/không đạt từ ngưỡng của
 * giống và lịch sử sự kiện thật; một kết quả đạt cần Brix >= ngưỡng, đủ cửa sổ cách ly và có bước
 * xác nhận tường minh của kỹ sư.
 */
@ApplicationScoped
public class QualityFacadeImpl implements QualityFacade {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String STATUS_HARVESTED = "HARVESTED";
    private static final String STATUS_QC_PASSED = "QC_PASSED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final ZoneId FARM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final BatchRepository batchRepository;
    private final CultivationEventRepository cultivationEventRepository;
    private final QcRecordRepository qcRecordRepository;
    private final VarietyRepository varietyRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final UserRepository userRepository;

    @Inject
    public QualityFacadeImpl(BatchRepository batchRepository,
            CultivationEventRepository cultivationEventRepository,
            QcRecordRepository qcRecordRepository,
            VarietyRepository varietyRepository,
            OutboxEventRepository outboxEventRepository,
            UserRepository userRepository) {
        this.batchRepository = batchRepository;
        this.cultivationEventRepository = cultivationEventRepository;
        this.qcRecordRepository = qcRecordRepository;
        this.varietyRepository = varietyRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public BaseResponse<BatchResponse> harvest(Long batchId, HarvestRequest request, Long actorId, boolean engineerOrAdmin) {
        if (request == null || request.clientEventId() == null || request.harvestedAt() == null
                || request.estimatedQuantity() == null) {
            throw new BadRequestException(ErrorCode.HARVEST_FIELDS_REQUIRED);
        }
        if (request.estimatedQuantity() < 1) {
            throw new BadRequestException(ErrorCode.HARVEST_QUANTITY_INVALID);
        }

        Batch batch = batchRepository.findByIdForUpdate(batchId);
        if (batch == null) throw new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND);
        requireAssignedOrEngineer(batchId, actorId, engineerOrAdmin);
        EventWriteGuard.requireBatchWritable(batch.getStatus());

        // Retry idempotent: clientEventId trùng sẽ trả về batch đã ghi trước đó.
        List<CultivationEvent> existing = cultivationEventRepository.findByClientEventId(request.clientEventId());
        if (!existing.isEmpty()) {
            CultivationEvent prior = existing.get(0);
            boolean sameOwner = prior.getBatch() != null && prior.getBatch().getId().equals(batchId);
            if (!sameOwner || !EventType.THU_HOACH.code().equals(prior.getEventType())) {
                throw new EventWriteGuard.ConflictException("clientEventId reused for a different batch or event");
            }
            return BaseResponse.build(toBatchView(batch));
        }

        if (STATUS_HARVESTED.equalsIgnoreCase(batch.getStatus())) {
            throw new EventWriteGuard.ConflictException("Batch is already harvested");
        }

        long recordedAt = System.currentTimeMillis();
        int seq = cultivationEventRepository.nextSeq(batchId);
        String payloadJson = harvestPayloadJson(request);
        String eventHash = EventHasher.eventHash(readTree(payloadJson), List.of());

        CultivationEvent event = CultivationEvent.builder()
                .batch(batch).seq(seq).eventType(EventType.THU_HOACH.code())
                .payloadJson(payloadJson).eventHash(eventHash)
                .status(EventStatus.CONFIRMED.name())
                .recordedBy(requireUser(actorId))
                .recordedAt(recordedAt).occurredAt(request.harvestedAt())
                .clientEventId(request.clientEventId()).build();
        cultivationEventRepository.persist(event);
        enqueuePrivateOutbox(batch, seq, eventHash);

        batch.setHarvestedAt(request.harvestedAt());
        batch.setHarvestDate(Instant.ofEpochMilli(request.harvestedAt()).atZone(FARM_ZONE).toLocalDate());
        batch.setStatus(STATUS_HARVESTED);
        batchRepository.merge(batch);

        return BaseResponse.build(toBatchView(batch));
    }

    @Override
    @Transactional
    public BaseResponse<QcResponse> recordQc(Long batchId, QcRequest request, Long actorId) {
        if (request == null || request.brix() == null || request.avgWeightKg() == null) {
            throw new BadRequestException(ErrorCode.QC_FIELDS_REQUIRED);
        }
        if (request.brix() < 0 || request.brix() > 30) throw new BadRequestException(ErrorCode.QC_BRIX_OUT_OF_RANGE);
        if (request.avgWeightKg() < 0.1 || request.avgWeightKg() > 20) {
            throw new BadRequestException(ErrorCode.QC_AVG_WEIGHT_OUT_OF_RANGE);
        }

        Batch batch = batchRepository.findByIdForUpdate(batchId);
        if (batch == null) throw new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND);
        EventWriteGuard.requireBatchWritable(batch.getStatus());
        if (batch.getHarvestedAt() == null) {
            throw new EventWriteGuard.ConflictException("Batch must be harvested before QC");
        }

        Variety variety = batch.getVariety() == null ? null : varietyRepository.findById(batch.getVariety().getId());
        Double minBrix = variety == null ? null : variety.getMinBrix();
        Integer minDays = variety == null ? null : variety.getMinIsolationDays();
        if (minBrix == null || minDays == null) {
            throw new ConflictException(ErrorCode.QC_MISSING_THRESHOLD);
        }

        List<CultivationEvent> events = cultivationEventRepository.findActiveByBatchId(batchId);
        IsolationPolicy.Result isolation = IsolationPolicy.evaluate(
                IsolationInputs.fromEvents(events), minDays, batch.getHarvestedAt());

        boolean brixOk = request.brix() >= minBrix;
        boolean isolationOk = isolation.outcome() == IsolationPolicy.Outcome.SUFFICIENT;
        boolean confirmed = Boolean.TRUE.equals(request.confirmPassed());

        if (confirmed && !isolationOk) {
            throw new ConflictException(ErrorCode.QC_ISOLATION_NOT_ENOUGH, List.of(isolation.outcome().toString()));
        }

        boolean passed = brixOk && isolationOk && confirmed;
        long now = System.currentTimeMillis();

        QcRecord record = QcRecord.builder()
                .batch(batch).brix(request.brix()).avgWeightKg(request.avgWeightKg())
                .passed(passed)
                .inspectedByUser(requireUser(actorId))
                .inspectedAt(now)
                .sizeDescription(request.sizeDescription()).note(request.note())
                .thresholdBrixUsed(minBrix).minIsolationDaysUsed(minDays)
                .isolationEligibleAt(isolation.eligibleAt()).harvestedAtUsed(batch.getHarvestedAt())
                .status(passed ? STATUS_QC_PASSED : STATUS_REJECTED)
                .superseded(false)
                .build();
        qcRecordRepository.persist(record);

        batch.setStatus(passed ? STATUS_QC_PASSED : STATUS_REJECTED);
        batchRepository.merge(batch);

        return BaseResponse.build(toQcView(record));
    }

    @Override
    @Transactional
    public BaseResponse<List<QcResponse>> listQc(Long batchId) {
        // Khóa dòng batch: revalidate có thể ghi (hạ pass quá hạn, đổi status) nên phải tuần tự hóa
        // cùng cơ chế với recordQc/harvest, tránh đua với các luồng ghi khác trên cùng lô.
        Batch batch = batchRepository.findByIdForUpdate(batchId);
        if (batch == null) throw new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND);
        revalidateStalePasses(batch);
        List<QcRecord> records = qcRecordRepository.findActiveByBatchIdOrderByIdDesc(batchId);
        return BaseResponse.build(records.stream().map(this::toQcView).toList());
    }

    /**
     * Tính lại cách ly từ nhật ký hiện tại và đánh dấu superseded cho mọi kết quả đạt còn hiệu lực
     * nhưng căn cứ đã thay đổi, đồng thời hạ batch ra khỏi QC_PASSED. Chạy trong transaction của
     * {@code listQc} nên chỉ ghi khi thực sự có bản ghi bị hạ.
     */
    private void revalidateStalePasses(Batch batch) {
        if (batch.getHarvestedAt() == null) return;
        List<QcRecord> records = qcRecordRepository.findActiveByBatchIdOrderByIdDesc(batch.getId());

        Variety variety = batch.getVariety() == null ? null : varietyRepository.findById(batch.getVariety().getId());
        Integer minDays = variety == null ? null : variety.getMinIsolationDays();
        List<CultivationEvent> events = cultivationEventRepository.findActiveByBatchId(batch.getId());
        IsolationPolicy.Result current = IsolationPolicy.evaluate(
                IsolationInputs.fromEvents(events), minDays, batch.getHarvestedAt());
        boolean demoted = false;
        for (QcRecord r : records) {
            boolean wasPassed = Boolean.TRUE.equals(r.getPassed()) && !Boolean.TRUE.equals(r.getSuperseded());
            if (QcStaleness.isStale(wasPassed, r.getIsolationEligibleAt(), current, batch.getHarvestedAt())) {
                r.setSuperseded(true);
                qcRecordRepository.merge(r);
                demoted = true;
            }
        }
        if (demoted && STATUS_QC_PASSED.equalsIgnoreCase(batch.getStatus())) {
            batch.setStatus(STATUS_HARVESTED);
            batchRepository.merge(batch);
        }
    }

    private void requireAssignedOrEngineer(Long batchId, Long actorId, boolean engineerOrAdmin) {
        if (engineerOrAdmin) return;
        if (actorId == null) throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
        if (batchRepository.countActiveAssignment(batchId, actorId) == 0) throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
    }

    private User requireUser(Long actorId) {
        return userRepository.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    private void enqueuePrivateOutbox(Batch batch, int seq, String eventHash) {
        OutboxEvent outbox = OutboxEvent.builder()
                .batchIdHash(EventHasher.batchIdHash(batch.getId(), batch.getSaltEnc()))
                .seq(seq).eventData(eventHash)
                .status(OutboxEvent.OutboxStatus.PENDING)
                .attempts(0).build();
        outboxEventRepository.persist(outbox);
    }

    private String harvestPayloadJson(HarvestRequest request) {
        try {
            var node = JSON.createObjectNode();
            node.put("estimatedQuantity", request.estimatedQuantity());
            if (request.area() != null) node.put("area", request.area());
            return JSON.writeValueAsString(node);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize harvest payload", e);
        }
    }

    private com.fasterxml.jackson.databind.JsonNode readTree(String json) {
        try {
            return JSON.readTree(json);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid harvest payload", e);
        }
    }

    private BatchResponse toBatchView(Batch batch) {
        return new BatchResponse(batch.getId(), batch.getBatchCode(),
                batch.getVariety() == null ? null : batch.getVariety().getId(),
                batch.getStatus(), batch.getHarvestedAt(),
                batch.getHarvestDate() == null ? null : batch.getHarvestDate().toString());
    }

    private QcResponse toQcView(QcRecord r) {
        return new QcResponse(r.getId(), r.getBatch() == null ? null : r.getBatch().getId(),
                r.getBrix(), r.getAvgWeightKg(), r.getPassed(), r.getStatus(),
                r.getInspectedByUser() == null ? null : r.getInspectedByUser().getId(),
                r.getInspectedAt(), r.getSizeDescription(), r.getNote(),
                r.getThresholdBrixUsed(), r.getMinIsolationDaysUsed(),
                r.getIsolationEligibleAt(), r.getHarvestedAtUsed(), r.getSuperseded());
    }
}
