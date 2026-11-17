package jakarta.ecofarm_backend.modules.log.facade.impl;

import jakarta.ecofarm_backend.common.dto.response.AttachmentResponse;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.EventResponse;
import jakarta.ecofarm_backend.common.dto.response.PageResponse;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.exception.BadRequestException;
import jakarta.ecofarm_backend.common.exception.EntityNotFoundException;
import jakarta.ecofarm_backend.common.exception.PermissionDeniedException;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.EventAttachment;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.BatchRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.CultivationEventRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.EventAttachmentRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.OutboxEventRepository;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.UserRepository;
import jakarta.ecofarm_backend.modules.log.ClockSkew;
import jakarta.ecofarm_backend.modules.log.EventHasher;
import jakarta.ecofarm_backend.modules.log.EventPayloadValidator;
import jakarta.ecofarm_backend.modules.log.EventRequests.CreateEventRequest;
import jakarta.ecofarm_backend.modules.log.EventStatus;
import jakarta.ecofarm_backend.modules.log.EventType;
import jakarta.ecofarm_backend.modules.log.EventWriteGuard;
import jakarta.ecofarm_backend.modules.log.LocalImageStore;
import jakarta.ecofarm_backend.modules.log.OutboxRouter;
import jakarta.ecofarm_backend.modules.log.facade.LogFacade;
import jakarta.ecofarm_backend.modules.log.service.AttachmentStorageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Logic nghiệp vụ cho nhật ký canh tác. Thao tác ghi khóa dòng batch (qua repository) và chạy trong
 * một transaction. Tải lên ghi tệp ra đĩa rồi lưu bản ghi DB trong cùng transaction; nếu lưu DB lỗi
 * thì dọn các tệp mồ côi đã ghi. Các hàm đọc cũng chạy trong transaction để duyệt quan hệ Lazy an toàn.
 */
@ApplicationScoped
public class LogFacadeImpl implements LogFacade {

    private final BatchRepository batchRepository;
    private final CultivationEventRepository cultivationEventRepository;
    private final EventAttachmentRepository eventAttachmentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final UserRepository userRepository;
    private final AttachmentStorageService storageService;

    @Inject
    public LogFacadeImpl(BatchRepository batchRepository,
            CultivationEventRepository cultivationEventRepository,
            EventAttachmentRepository eventAttachmentRepository,
            OutboxEventRepository outboxEventRepository,
            UserRepository userRepository,
            AttachmentStorageService storageService) {
        this.batchRepository = batchRepository;
        this.cultivationEventRepository = cultivationEventRepository;
        this.eventAttachmentRepository = eventAttachmentRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    @Override
    @Transactional
    public BaseResponse<List<AttachmentResponse>> upload(Long batchId, List<UploadInput> files, Long actorId, boolean engineerOrAdmin) throws IOException {
        if (files != null && files.size() > 5) throw new BadRequestException(ErrorCode.TOO_MANY_ATTACHMENTS);

        List<LocalImageStore.StoredImage> stored = new ArrayList<>();
        try {
            if (files != null) {
                for (UploadInput f : files) {
                    try (var in = new ByteArrayInputStream(f.bytes() == null ? new byte[0] : f.bytes())) {
                        stored.add(storageService.save(in, f.mimeType()));
                    } catch (IllegalArgumentException e) {
                        throw new BadRequestException(ErrorCode.INVALID_ATTACHMENT, List.of(e.getMessage()));
                    }
                }
            }

            Batch batch = batchRepository.findByIdForUpdate(batchId);
            if (batch == null) throw new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND);
            requireAssignedOrEngineer(batchId, actorId, engineerOrAdmin);
            EventWriteGuard.requireBatchWritable(batch.getStatus());

            List<AttachmentResponse> created = new ArrayList<>();
            for (LocalImageStore.StoredImage s : stored) {
                EventAttachment attachment = EventAttachment.builder()
                        .fileKey(s.fileKey()).sha256(s.sha256()).mimeType(s.mimeType()).build();
                eventAttachmentRepository.persist(attachment);
                created.add(AttachmentResponse.from(attachment));
            }
            return BaseResponse.build(created);
        } catch (RuntimeException | IOException e) {
            for (LocalImageStore.StoredImage s : stored) storageService.deleteQuietly(s.fileKey());
            throw e;
        }
    }

    @Override
    @Transactional
    public DownloadResult download(Long batchId, String fileKey, Long actorId, boolean engineerOrAdmin) throws IOException {
        Batch batch = batchRepository.findById(batchId);
        if (batch == null) throw new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND);
        requireAssignedOrEngineer(batchId, actorId, engineerOrAdmin);
        List<EventAttachment> found = eventAttachmentRepository.findActiveByFileKey(fileKey);
        if (found.isEmpty()) throw new EntityNotFoundException(ErrorCode.ATTACHMENT_NOT_FOUND);
        EventAttachment attachment = found.get(0);
        if (attachment.getEvent() != null && !attachment.getEvent().getBatch().getId().equals(batchId)) {
            throw new PermissionDeniedException(ErrorCode.ATTACHMENT_NOT_IN_BATCH);
        }
        byte[] bytes;
        try {
            bytes = storageService.read(fileKey);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(ErrorCode.INVALID_ATTACHMENT, List.of(e.getMessage()));
        }
        return new DownloadResult(bytes, attachment.getMimeType());
    }

    @Override
    @Transactional
    public BaseResponse<EventResponse> create(Long batchId, CreateEventRequest request, Long actorId, boolean engineerOrAdmin, long clockSkewThresholdMs) {
        if (request == null || request.eventType() == null || request.payload() == null) {
            throw new BadRequestException(ErrorCode.INVALID_EVENT_PAYLOAD);
        }
        EventType type = EventType.fromCode(request.eventType())
                .filter(EventType::isClientRecordable)
                .orElseThrow(() -> new BadRequestException(ErrorCode.UNSUPPORTED_EVENT_TYPE));
        EventPayloadValidator.validate(type, request.payload());

        // Khóa dòng batch để việc cấp seq và kiểm tra trạng thái khóa được tuần tự hóa theo từng lô.
        Batch batch = batchRepository.findByIdForUpdate(batchId);
        if (batch == null) throw new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND);
        requireAssignedOrEngineer(batchId, actorId, engineerOrAdmin);
        EventWriteGuard.requireBatchWritable(batch.getStatus());

        // Retry idempotent: clientEventId trùng sẽ trả về sự kiện đã lưu thay vì tạo trùng.
        if (request.clientEventId() != null) {
            List<CultivationEvent> existing = cultivationEventRepository.findByClientEventId(request.clientEventId());
            if (!existing.isEmpty()) {
                CultivationEvent prior = existing.get(0);
                boolean sameOwner = prior.getRecordedBy() != null && prior.getRecordedBy().getId().equals(actorId)
                        && prior.getBatch().getId().equals(batchId);
                String expectedHash = EventHasher.eventHash(request.payload(), attachmentHashes(request.attachmentFileKeys()));
                EventWriteGuard.requireRetryMatches(sameOwner, expectedHash.equals(prior.getEventHash()));
                return BaseResponse.build(toView(prior));
            }
        }

        List<EventAttachment> attachments = resolveAttachments(request.attachmentFileKeys());
        String eventHash = EventHasher.eventHash(request.payload(),
                attachments.stream().map(EventAttachment::getSha256).toList());
        int seq = cultivationEventRepository.nextSeq(batchId);

        long recordedAt = System.currentTimeMillis();
        Long skew = ClockSkew.skewMs(request.deviceSentAt(), recordedAt);
        CultivationEvent event = CultivationEvent.builder()
                .batch(batch).seq(seq).eventType(type.code())
                .payloadJson(request.payload().toString()).eventHash(eventHash)
                .status(EventStatus.PENDING.name()).recordedBy(requireUser(actorId))
                .recordedAt(recordedAt).occurredAt(request.occurredAt())
                .deviceClockSkewMs(skew)
                .needsReview(ClockSkew.needsReview(skew, clockSkewThresholdMs))
                .clientEventId(request.clientEventId()).build();
        cultivationEventRepository.persist(event);
        for (EventAttachment a : attachments) { a.setEvent(event); eventAttachmentRepository.merge(a); }
        enqueuePrivateOutbox(batch, seq, type, eventHash);
        return BaseResponse.build(toView(event));
    }

    @Override
    @Transactional
    public BaseResponse<PageResponse<EventResponse>> list(Long batchId, int page, int size, String status, String eventType,
            boolean includeSuperseded, Long actorId, boolean engineerOrAdmin, boolean workerOrFarmer) {
        if (batchRepository.findById(batchId) == null) throw new EntityNotFoundException(ErrorCode.BATCH_NOT_FOUND);
        if (!engineerOrAdmin && workerOrFarmer) {
            if (actorId == null) throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
            if (!batchRepository.isAssigned(batchId, actorId)) throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
        }
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(100, size));
        long offset = (long) safePage * safeSize;
        long total = cultivationEventRepository.countByBatchFiltered(batchId, status, eventType, includeSuperseded);
        List<CultivationEvent> events = offset > Integer.MAX_VALUE ? List.of()
                : cultivationEventRepository.findPageByBatchFiltered(batchId, status, eventType, includeSuperseded, (int) offset, safeSize);
        List<Long> eventIds = events.stream().map(CultivationEvent::getId).toList();
        List<EventAttachment> allAttachments = eventAttachmentRepository.findActiveByEventIdsOrderById(eventIds);
        List<EventResponse> content = events.stream().map(event -> {
            List<AttachmentResponse> views = allAttachments.stream()
                    .filter(a -> a.getEvent() != null && a.getEvent().getId().equals(event.getId()))
                    .map(AttachmentResponse::from).toList();
            return EventResponse.from(event, views);
        }).toList();
        return BaseResponse.build(new PageResponse<>(content, safePage, safeSize, total));
    }

    private void requireAssignedOrEngineer(Long batchId, Long actorId, boolean engineerOrAdmin) {
        if (engineerOrAdmin) return;
        if (actorId == null) throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
        if (!batchRepository.isAssigned(batchId, actorId)) throw new PermissionDeniedException(ErrorCode.PERMISSION_DENIED);
    }

    private User requireUser(Long actorId) {
        return userRepository.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    private List<String> attachmentHashes(List<String> fileKeys) {
        return eventAttachmentRepository.findSha256ByFileKeys(fileKeys);
    }

    /** Tệp đính kèm phải được tải lên (và chưa gắn sự kiện) trước khi sự kiện tham chiếu tới. */
    private List<EventAttachment> resolveAttachments(List<String> fileKeys) {
        List<EventAttachment> result = new ArrayList<>();
        if (fileKeys == null) return result;
        if (fileKeys.size() > 5) throw new BadRequestException(ErrorCode.TOO_MANY_ATTACHMENTS);
        for (String key : fileKeys) {
            List<EventAttachment> found = eventAttachmentRepository.findUnlinkedByFileKey(key);
            if (found.isEmpty()) throw new BadRequestException(ErrorCode.ATTACHMENT_NOT_LINKABLE, List.of(key));
            result.add(found.get(0));
        }
        return result;
    }

    private void enqueuePrivateOutbox(Batch batch, int seq, EventType type, String eventHash) {
        if (OutboxRouter.reachesPublicChain(type)) {
            throw new IllegalStateException("Sự kiện LOG không bao giờ được nhắm tới chuỗi công khai");
        }
        OutboxEvent outbox = OutboxEvent.builder()
                .batchIdHash(EventHasher.batchIdHash(batch.getId(), batch.getSaltEnc()))
                .seq(seq).eventData(eventHash)
                .status(OutboxEvent.OutboxStatus.PENDING)
                .attempts(0).build();
        outboxEventRepository.persist(outbox);
    }

    private EventResponse toView(CultivationEvent event) {
        List<AttachmentResponse> attachments = eventAttachmentRepository.findActiveByEventIdOrderById(event.getId())
                .stream().map(AttachmentResponse::from).toList();
        return EventResponse.from(event, attachments);
    }
}
