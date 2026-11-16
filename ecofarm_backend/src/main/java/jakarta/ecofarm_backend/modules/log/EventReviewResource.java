package jakarta.ecofarm_backend.modules.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent;
import jakarta.ecofarm_backend.infrastructure.security.UserSecurityDetailsPrincipal;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import jakarta.ecofarm_backend.modules.log.EventRequests.AmendRequest;
import jakarta.ecofarm_backend.modules.log.EventRequests.RejectRequest;

/**
 * Engineer review endpoints for a recorded event, rooted at {@code /v1/events/{eventId}} per the API
 * contract (separate from the batch-scoped create endpoint).
 *
 * <p>Approval and rejection are restricted to engineers and act only on a chain-confirmed event.
 * Approval adds exactly one system {@code EVENT_APPROVED} event referencing the original hash and
 * never mutates the original. An amendment creates a new event and marks the prior one
 * {@code SUPERSEDED}, keeping the old payload and hash intact. Each path runs in the container
 * transaction so the status change, the new event and its outbox row commit together or not at all.
 */
@Path("/v1/events/{eventId}")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
@RequireRoles({"ADMIN", "ENGINEER", "WORKER", "FARMER", "PACKER", "REGULATOR"})
public class EventReviewResource {

    private static final ObjectMapper JSON = new ObjectMapper();

    @PersistenceContext
    private EntityManager em;

    @Context
    private SecurityContext securityContext;

    @POST
    @Path("/approve")
    public Response approve(@PathParam("eventId") Long eventId) {
        requireEngineer();
        CultivationEvent event = lockEvent(eventId);
        EventWriteGuard.requireBatchWritable(event.getBatch().getStatus());
        EventWriteGuard.requireApprovable(EventStatus.valueOf(event.getStatus()));

        // Idempotent: a second approve does not create another EVENT_APPROVED.
        Long alreadyApproved = em.createQuery(
                        "select count(e) from CultivationEvent e where e.amendsEvent.id = :id and e.eventType = :t", Long.class)
                .setParameter("id", eventId).setParameter("t", EventType.EVENT_APPROVED.code()).getSingleResult();
        if (alreadyApproved == 0) {
            int seq = nextSeq(event.getBatch().getId());
            String hash = EventHasher.eventHash(JSON.createObjectNode()
                    .put("schemaVersion", 1).put("approvedEventHash", event.getEventHash()), java.util.List.of());
            CultivationEvent approval = CultivationEvent.builder()
                    .batch(event.getBatch()).seq(seq).eventType(EventType.EVENT_APPROVED.code())
                    .payloadJson("{\"approvedEventHash\":\"" + event.getEventHash() + "\"}")
                    .eventHash(hash).status(EventStatus.PENDING.name())
                    .recordedBy(event.getRecordedBy()).recordedAt(System.currentTimeMillis())
                    .occurredAt(System.currentTimeMillis()).amendsEvent(event).build();
            em.persist(approval);
            enqueuePrivate(event.getBatch(), seq, hash);
        }
        event.setStatus(EventStatus.APPROVED.name());
        return Response.ok(BaseResponse.ok()).build();
    }

    @POST
    @Path("/reject")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reject(@PathParam("eventId") Long eventId, RejectRequest request) {
        requireEngineer();
        if (request == null || request.reason() == null || request.reason().isBlank()) {
            throw new BadRequestException("A rejection reason is required");
        }
        CultivationEvent event = lockEvent(eventId);
        EventWriteGuard.requireBatchWritable(event.getBatch().getStatus());
        EventWriteGuard.requireApprovable(EventStatus.valueOf(event.getStatus()));
        // The original content is left intact; only the status changes.
        event.setStatus(EventStatus.REJECTED.name());
        return Response.ok(BaseResponse.ok()).build();
    }

    @POST
    @Path("/amend")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response amend(@PathParam("eventId") Long eventId, AmendRequest request) {
        UserSecurityDetailsPrincipal user = requireUser();
        CultivationEvent prior = lockEvent(eventId);
        EventWriteGuard.requireBatchWritable(prior.getBatch().getStatus());
        EventWriteGuard.requireAmendable(EventStatus.valueOf(prior.getStatus()));
        EventType type = EventType.fromCode(prior.getEventType())
                .orElseThrow(() -> new IllegalStateException("Stored event has unknown type"));
        EventPayloadValidator.validate(type, request.payload());

        int seq = nextSeq(prior.getBatch().getId());
        String hash = EventHasher.eventHash(request.payload(), java.util.List.of());
        CultivationEvent amended = CultivationEvent.builder()
                .batch(prior.getBatch()).seq(seq).eventType(prior.getEventType())
                .payloadJson(request.payload().toString()).eventHash(hash)
                .status(EventStatus.PENDING.name())
                .recordedBy(em.getReference(jakarta.ecofarm_backend.infrastructure.persistence.entity.User.class, user.getId()))
                .recordedAt(System.currentTimeMillis()).occurredAt(request.occurredAt())
                .clientEventId(request.clientEventId()).amendsEvent(prior).build();
        em.persist(amended);
        // The prior payload and hash are untouched; only its status moves to SUPERSEDED.
        prior.setStatus(EventStatus.SUPERSEDED.name());
        enqueuePrivate(prior.getBatch(), seq, hash);
        return Response.status(Response.Status.CREATED).entity(BaseResponse.ok()).build();
    }

    private CultivationEvent lockEvent(Long eventId) {
        CultivationEvent event = em.find(CultivationEvent.class, eventId, LockModeType.PESSIMISTIC_WRITE);
        if (event == null) throw new NotFoundException();
        return event;
    }

    private void requireEngineer() {
        if (!securityContext.isUserInRole("ADMIN") && !securityContext.isUserInRole("ENGINEER")) {
            throw new ForbiddenException("Only an engineer may approve or reject");
        }
    }

    private UserSecurityDetailsPrincipal requireUser() {
        if (!(securityContext.getUserPrincipal() instanceof UserSecurityDetailsPrincipal user)) {
            throw new ForbiddenException();
        }
        return user;
    }

    private int nextSeq(Long batchId) {
        Integer maxSeq = em.createQuery(
                        "select max(e.seq) from CultivationEvent e where e.batch.id = :batchId", Integer.class)
                .setParameter("batchId", batchId).getSingleResult();
        return maxSeq == null ? 1 : maxSeq + 1;
    }

    private void enqueuePrivate(Batch batch, int seq, String eventHash) {
        OutboxEvent outbox = OutboxEvent.builder()
                .batchIdHash(EventHasher.batchIdHash(batch.getId(), batch.getSaltEnc()))
                .seq(seq).eventData(eventHash).status(OutboxEvent.OutboxStatus.PENDING).attempts(0).build();
        em.persist(outbox);
    }
}
