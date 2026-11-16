package jakarta.ecofarm_backend.modules.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.PageResponse;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.EventAttachment;
import jakarta.ecofarm_backend.infrastructure.security.UserSecurityDetailsPrincipal;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.ecofarm_backend.modules.log.EventRequests.AmendRequest;
import jakarta.ecofarm_backend.modules.log.EventRequests.CreateEventRequest;
import jakarta.ecofarm_backend.modules.log.EventRequests.RejectRequest;

@Path("/v1/batches/{batchId}/events")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
@RequireRoles({"ADMIN", "ENGINEER", "WORKER", "FARMER", "PACKER", "REGULATOR"})
public class CultivationEventResource {
    private static final ObjectMapper JSON = new ObjectMapper();
    @PersistenceContext
    private EntityManager em;

    @Context
    private SecurityContext securityContext;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.log.attachment.root", defaultValue = "/var/lib/ecofarm/log-attachments")
    private String attachmentRoot;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.log.attachment.max-bytes", defaultValue = "5242880")
    private int attachmentMaxBytes;

    private LocalImageStore store() {
        return new LocalImageStore(java.nio.file.Path.of(attachmentRoot), attachmentMaxBytes);
    }

    /**
     * Upload an attachment for a batch before referencing it from an event. The server computes the
     * SHA-256 and validates real content; the returned fileKey is later linked by {@code create}.
     */
    @POST
    @Path("/attachments")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(@PathParam("batchId") Long batchId, List<EntityPart> parts) throws IOException {
        UserSecurityDetailsPrincipal user = requireUser();
        Batch batch = em.find(Batch.class, batchId, LockModeType.PESSIMISTIC_WRITE);
        if (batch == null) throw new NotFoundException();
        requireAssignedOrEngineer(batch, user);
        EventWriteGuard.requireBatchWritable(batch.getStatus());

        List<AttachmentView> created = new ArrayList<>();
        for (EntityPart part : parts) {
            if (created.size() >= 5) throw new BadRequestException("At most 5 attachments per upload");
            String mime = part.getMediaType() == null ? null : part.getMediaType().toString();
            LocalImageStore.StoredImage stored;
            try (InputStream in = part.getContent()) {
                stored = store().save(in, mime);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(e.getMessage());
            }
            EventAttachment attachment = EventAttachment.builder()
                    .fileKey(stored.fileKey()).sha256(stored.sha256()).mimeType(stored.mimeType()).build();
            em.persist(attachment);
            created.add(AttachmentView.from(attachment));
        }
        return Response.status(Response.Status.CREATED).entity(BaseResponse.build(created)).build();
    }

    /** Download an attachment's bytes, checking the caller may read the owning batch. */
    @GET
    @Path("/attachments/{fileKey}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response download(@PathParam("batchId") Long batchId, @PathParam("fileKey") String fileKey) throws IOException {
        UserSecurityDetailsPrincipal user = requireUser();
        Batch batch = em.find(Batch.class, batchId);
        if (batch == null) throw new NotFoundException();
        requireAssignedOrEngineer(batch, user);
        List<EventAttachment> found = em.createQuery(
                        "select a from EventAttachment a where a.fileKey = :key and a.isActive = true", EventAttachment.class)
                .setParameter("key", fileKey).getResultList();
        if (found.isEmpty()) throw new NotFoundException();
        EventAttachment attachment = found.get(0);
        if (attachment.getEvent() != null && !attachment.getEvent().getBatch().getId().equals(batchId)) {
            throw new ForbiddenException("Attachment does not belong to this batch");
        }
        byte[] bytes;
        try {
            bytes = store().read(fileKey);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
        return Response.ok(bytes).type(attachment.getMimeType()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@PathParam("batchId") Long batchId, CreateEventRequest request) {
        UserSecurityDetailsPrincipal user = requireUser();
        EventType type = EventType.fromCode(request.eventType())
                .filter(EventType::isClientRecordable)
                .orElseThrow(() -> new BadRequestException("Unsupported or non-recordable event type"));
        EventPayloadValidator.validate(type, request.payload());

        // Lock the batch row so seq assignment and the lock-state check are serialized per batch.
        Batch batch = em.find(Batch.class, batchId, LockModeType.PESSIMISTIC_WRITE);
        if (batch == null) throw new NotFoundException();
        requireAssignedOrEngineer(batch, user);
        EventWriteGuard.requireBatchWritable(batch.getStatus());

        // Idempotent retry: a matching clientEventId returns the stored event instead of duplicating.
        if (request.clientEventId() != null) {
            List<CultivationEvent> existing = em.createQuery(
                            "select e from CultivationEvent e where e.clientEventId = :cid", CultivationEvent.class)
                    .setParameter("cid", request.clientEventId()).getResultList();
            if (!existing.isEmpty()) {
                CultivationEvent prior = existing.get(0);
                boolean sameOwner = prior.getRecordedBy() != null && prior.getRecordedBy().getId().equals(user.getId())
                        && prior.getBatch().getId().equals(batchId);
                String expectedHash = EventHasher.eventHash(request.payload(), attachmentHashes(request.attachmentFileKeys()));
                EventWriteGuard.requireRetryMatches(sameOwner, expectedHash.equals(prior.getEventHash()));
                return Response.ok(BaseResponse.build(toView(prior))).build();
            }
        }

        List<EventAttachment> attachments = resolveAttachments(request.attachmentFileKeys(), batchId, user);
        String eventHash = EventHasher.eventHash(request.payload(),
                attachments.stream().map(EventAttachment::getSha256).toList());
        int seq = nextSeq(batchId);

        CultivationEvent event = CultivationEvent.builder()
                .batch(batch).seq(seq).eventType(type.code())
                .payloadJson(request.payload().toString()).eventHash(eventHash)
                .status(EventStatus.PENDING.name()).recordedBy(em.getReference(
                        jakarta.ecofarm_backend.infrastructure.persistence.entity.User.class, user.getId()))
                .recordedAt(System.currentTimeMillis()).occurredAt(request.occurredAt())
                .clientEventId(request.clientEventId()).build();
        em.persist(event);
        for (EventAttachment a : attachments) { a.setEvent(event); em.persist(a); }
        enqueuePrivateOutbox(batch, seq, type, eventHash);
        return Response.status(Response.Status.CREATED).entity(BaseResponse.build(toView(event))).build();
    }

    // --- helpers shared by the write paths ---

    private UserSecurityDetailsPrincipal requireUser() {
        if (!(securityContext.getUserPrincipal() instanceof UserSecurityDetailsPrincipal user)) {
            throw new ForbiddenException();
        }
        return user;
    }

    private void requireAssignedOrEngineer(Batch batch, UserSecurityDetailsPrincipal user) {
        if (securityContext.isUserInRole("ADMIN") || securityContext.isUserInRole("ENGINEER")) return;
        Long assigned = em.createQuery(
                        "select count(a) from BatchAssignment a where a.batch.id = :batchId and a.user.id = :userId and a.isActive = true",
                        Long.class)
                .setParameter("batchId", batch.getId()).setParameter("userId", user.getId()).getSingleResult();
        if (assigned == 0) throw new ForbiddenException();
    }

    private int nextSeq(Long batchId) {
        Integer maxSeq = em.createQuery(
                        "select max(e.seq) from CultivationEvent e where e.batch.id = :batchId", Integer.class)
                .setParameter("batchId", batchId).getSingleResult();
        return maxSeq == null ? 1 : maxSeq + 1;
    }

    private List<String> attachmentHashes(List<String> fileKeys) {
        if (fileKeys == null || fileKeys.isEmpty()) return List.of();
        return em.createQuery(
                        "select a.sha256 from EventAttachment a where a.fileKey in :keys", String.class)
                .setParameter("keys", fileKeys).getResultList();
    }

    /** Attachments must already be uploaded (and owned by this batch) before the event references them. */
    private List<EventAttachment> resolveAttachments(List<String> fileKeys, Long batchId, UserSecurityDetailsPrincipal user) {
        List<EventAttachment> result = new ArrayList<>();
        if (fileKeys == null) return result;
        if (fileKeys.size() > 5) throw new BadRequestException("At most 5 attachments per event");
        for (String key : fileKeys) {
            List<EventAttachment> found = em.createQuery(
                            "select a from EventAttachment a where a.fileKey = :key and a.event is null", EventAttachment.class)
                    .setParameter("key", key).getResultList();
            if (found.isEmpty()) throw new BadRequestException("Unknown or already-linked attachment: " + key);
            result.add(found.get(0));
        }
        return result;
    }

    private void enqueuePrivateOutbox(Batch batch, int seq, EventType type, String eventHash) {
        if (OutboxRouter.reachesPublicChain(type)) {
            throw new IllegalStateException("LOG events must never target the public chain");
        }
        jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent outbox =
                jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent.builder()
                        .batchIdHash(EventHasher.batchIdHash(batch.getId(), batch.getSaltEnc()))
                        .seq(seq).eventData(eventHash)
                        .status(jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent.OutboxStatus.PENDING)
                        .attempts(0).build();
        em.persist(outbox);
    }

    private EventView toView(CultivationEvent event) {
        List<AttachmentView> attachments = em.createQuery(
                        "select a from EventAttachment a where a.event.id = :id and a.isActive = true order by a.id", EventAttachment.class)
                .setParameter("id", event.getId()).getResultList().stream().map(AttachmentView::from).toList();
        return EventView.from(event, attachments);
    }

    @GET
    public BaseResponse<PageResponse<EventView>> list(@PathParam("batchId") Long batchId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("status") String status,
            @QueryParam("eventType") String eventType,
            @QueryParam("includeSuperseded") @DefaultValue("true") boolean includeSuperseded) {
        if (em.find(Batch.class, batchId) == null) throw new NotFoundException();
        if (!securityContext.isUserInRole("ADMIN") && !securityContext.isUserInRole("ENGINEER")
                && (securityContext.isUserInRole("WORKER") || securityContext.isUserInRole("FARMER"))) {
            if (!(securityContext.getUserPrincipal() instanceof UserSecurityDetailsPrincipal user)) throw new ForbiddenException();
            Long assigned = em.createQuery("select count(a) from BatchAssignment a where a.batch.id = :batchId and a.user.id = :userId and a.isActive = true", Long.class)
                    .setParameter("batchId", batchId).setParameter("userId", user.getId()).getSingleResult();
            if (assigned == 0) throw new ForbiddenException();
        }
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(100, size));
        String where = " where e.batch.id = :batchId and e.isActive = true"
                + (status == null ? "" : " and e.status = :status")
                + (eventType == null ? "" : " and e.eventType = :eventType")
                + (includeSuperseded ? "" : " and e.status <> 'SUPERSEDED'");
        var count = em.createQuery("select count(e) from CultivationEvent e" + where, Long.class).setParameter("batchId", batchId);
        var query = em.createQuery("select e from CultivationEvent e" + where + " order by e.seq asc, e.id asc", CultivationEvent.class).setParameter("batchId", batchId);
        if (status != null) { count.setParameter("status", status); query.setParameter("status", status); }
        if (eventType != null) { count.setParameter("eventType", eventType); query.setParameter("eventType", eventType); }
        long offset = (long) safePage * safeSize;
        List<CultivationEvent> events = offset > Integer.MAX_VALUE ? List.of() : query.setFirstResult((int) offset).setMaxResults(safeSize).getResultList();
        Map<Long, List<AttachmentView>> attachments = events.isEmpty() ? Map.of() : em.createQuery(
                        "select a from EventAttachment a where a.event.id in :eventIds and a.isActive = true order by a.id", EventAttachment.class)
                .setParameter("eventIds", events.stream().map(CultivationEvent::getId).toList()).getResultList().stream()
                .collect(Collectors.groupingBy(a -> a.getEvent().getId(), Collectors.mapping(AttachmentView::from, Collectors.toList())));
        List<EventView> content = events.stream().map(event -> EventView.from(event, attachments.getOrDefault(event.getId(), List.of()))).toList();
        return BaseResponse.build(new PageResponse<>(content, safePage, safeSize, count.getSingleResult()));
    }

    public record EventView(Long id, Long batchId, Integer seq, String eventType, JsonNode payload, String eventHash, String status,
            Long recordedBy, Long recordedAt, Long occurredAt, String clientEventId, Long amendsEventId, List<AttachmentView> attachments) {
        static EventView from(CultivationEvent event, List<AttachmentView> attachments) {
            JsonNode payload;
            try {
                payload = event.getPayloadJson() == null ? null : JSON.readTree(event.getPayloadJson());
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Invalid stored cultivation event payload", e);
            }
            return new EventView(event.getId(), event.getBatch().getId(), event.getSeq(), event.getEventType(), payload, event.getEventHash(), event.getStatus(),
                    event.getRecordedBy() == null ? null : event.getRecordedBy().getId(), event.getRecordedAt(), event.getOccurredAt(), event.getClientEventId(),
                    event.getAmendsEvent() == null ? null : event.getAmendsEvent().getId(), attachments);
        }
    }

    public record AttachmentView(Long id, String fileKey, String sha256, String mimeType) {
        static AttachmentView from(EventAttachment attachment) {
            return new AttachmentView(attachment.getId(), attachment.getFileKey(), attachment.getSha256(), attachment.getMimeType());
        }
    }
}
