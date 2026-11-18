package jakarta.ecofarm_backend.modules.acttachment.facade.impl;

import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.exception.EntityNotFoundException;
import jakarta.ecofarm_backend.common.exception.InternalServerException;
import jakarta.ecofarm_backend.common.exception.PermissionDeniedException;
import jakarta.ecofarm_backend.common.util.ByteArrayPart;
import jakarta.ecofarm_backend.common.util.CurrentUserUtil;
import jakarta.ecofarm_backend.common.util.MimeTypeVerifier;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.EventAttachment;
import jakarta.ecofarm_backend.infrastructure.security.UserSecurityDetailsPrincipal;
import jakarta.ecofarm_backend.infrastructure.service.BatchService;
import jakarta.ecofarm_backend.infrastructure.service.CultivationEventService;
import jakarta.ecofarm_backend.infrastructure.service.EventAttachmentService;
import jakarta.ecofarm_backend.infrastructure.service.StorageService;
import jakarta.ecofarm_backend.modules.acttachment.dto.CreateEventAttachmentRequest;
import jakarta.ecofarm_backend.modules.acttachment.facade.EventAttachmentFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.Part;
import jakarta.transaction.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@ApplicationScoped
public class EventAttachmentFacadeImpl implements EventAttachmentFacade {

    @Inject
    private CurrentUserUtil currentUser;

    @Inject
    private CultivationEventService cultivationEventService;

    @Inject
    private EventAttachmentService eventAttachmentService;

    @Inject
    private BatchService batchService;

    @Inject
    private StorageService storageService;


    @Override
    @Transactional
    public BaseResponse<Boolean> createAttachment(CreateEventAttachmentRequest request, Part file) {
        UserSecurityDetailsPrincipal principal = currentUser.get();

        CultivationEvent event = findActiveEvent(request.getEventId());

        Long batchId = event.getBatch() == null ? null : event.getBatch().getId();
        requireAssigned(batchId, principal.getId());

        MimeTypeVerifier.validateFile(file);

        byte[] fileBytes = readBytes(file);
        String declaredMime = file.getContentType();
        MimeTypeVerifier.verify(declaredMime, fileBytes);

        String sha256 = sha256Hex(fileBytes);

        ByteArrayPart byteArrayPart = ByteArrayPart.builder().original(file).data(fileBytes).build();

        String fileKey = storageService.upload(byteArrayPart);

        EventAttachment attachment = EventAttachment.builder()
                .event(event)
                .fileKey(fileKey)
                .sha256(sha256)
                .mimeType(declaredMime)
                .capturedAt(request.getCapturedAt())
                .gpsLat(request.getGpsLat())
                .gpsLng(request.getGpsLng())
                .gpsAccuracyM(request.getGpsAccuracyM())
                .build();

        eventAttachmentService.persist(attachment);

        return BaseResponse.build(true);
    }

    private CultivationEvent findActiveEvent(Long eventId) {
        return cultivationEventService.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.EVENT_NOT_FOUND));
    }

    private void requireAssigned(Long batchId, Long userId) {
        boolean isUserAssignedBatch = batchService.isAssigned(batchId, userId);
        if (!isUserAssignedBatch) {
            throw new PermissionDeniedException(ErrorCode.BATCH_NOT_ASSIGNED);
        }
    }

    private byte[] readBytes(Part file) {
        try (InputStream is = file.getInputStream()) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new InternalServerException(ErrorCode.FILE_READ_ERROR, e);
        }
    }

    private String sha256Hex(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalServerException(ErrorCode.SYSTEM_HASH_ERROR, e);
        }
    }
}
