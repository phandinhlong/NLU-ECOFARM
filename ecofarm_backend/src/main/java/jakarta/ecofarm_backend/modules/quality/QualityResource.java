package jakarta.ecofarm_backend.modules.quality;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ecofarm_backend.common.anotation.RequireRoles;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.OutboxEvent;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.QcRecord;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.User;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Variety;
import jakarta.ecofarm_backend.infrastructure.security.UserSecurityDetailsPrincipal;
import jakarta.ecofarm_backend.modules.log.EventHasher;
import jakarta.ecofarm_backend.modules.log.EventStatus;
import jakarta.ecofarm_backend.modules.log.EventType;
import jakarta.ecofarm_backend.modules.log.EventWriteGuard;
import jakarta.ecofarm_backend.modules.quality.QualityRequests.HarvestRequest;
import jakarta.ecofarm_backend.modules.quality.QualityRequests.QcRequest;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

/**
 * Harvest and quality-control endpoints for a batch.
 *
 * <p>Both writes lock the batch row and run in one transaction. Harvest records a system-generated
 * {@code THU_HOACH} event through the same journal mechanism (sequence, hash, private outbox) so a
 * harvest can never diverge from the journal, then flips the batch to {@code HARVESTED}.
 * QC never trusts a client pass flag: the server recomputes the result from the variety thresholds
 * and the real event history, and a pass requires Brix at/above threshold, a satisfied isolation
 * window and the engineer's explicit {@code confirmPassed}. An isolation violation returns 422.
 */
@Path("/v1/batches/{batchId}")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
@RequireRoles({"ADMIN", "ENGINEER", "WORKER"})
public class QualityResource {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String STATUS_HARVESTED = "HARVESTED";
    private static final String STATUS_QC_PASSED = "QC_PASSED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final ZoneId FARM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @PersistenceContext
    private EntityManager em;

    @Context
    private SecurityContext securityContext;

    // ----------------------------------------------------------------------------- Harvest

    @POST
    @Path("/harvest")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response harvest(@PathParam("batchId") Long batchId, HarvestRequest request) {
        UserSecurityDetailsPrincipal user = requireUser();
        if (request == null || request.clientEventId() == null || request.harvestedAt() == null
                || request.estimatedQuantity() == null) {
            throw new BadRequestException("clientEventId, harvestedAt and estimatedQuantity are required");
        }
        if (request.estimatedQuantity() < 1) {
            throw new BadRequestException("estimatedQuantity must be positive");
        }

        Batch batch = em.find(Batch.class, batchId, LockModeType.PESSIMISTIC_WRITE);
        if (batch == null) throw new NotFoundException();
        requireAssignedOrEngineer(batchId);
        EventWriteGuard.requireBatchWritable(batch.getStatus());

        // Idempotent retry: a matching clientEventId returns the already-recorded batch.
        List<CultivationEvent> existing = em.createQuery(
                        "select e from CultivationEvent e where e.clientEventId = :cid", CultivationEvent.class)
                .setParameter("cid", request.clientEventId()).getResultList();
        if (!existing.isEmpty()) {
            CultivationEvent prior = existing.get(0);
            boolean sameOwner = prior.getBatch() != null && prior.getBatch().getId().equals(batchId);
            if (!sameOwner || !EventType.THU_HOACH.code().equals(prior.getEventType())) {
                throw new EventWriteGuard.ConflictException("clientEventId reused for a different batch or event");
            }
            return Response.status(Response.Status.CREATED).entity(BaseResponse.build(toBatchView(batch))).build();
        }

        if (STATUS_HARVESTED.equalsIgnoreCase(batch.getStatus())) {
            throw new EventWriteGuard.ConflictException("Batch is already harvested");
        }

        long recordedAt = System.currentTimeMillis();
        int seq = nextSeq(batchId);
        String payloadJson = harvestPayloadJson(request);
        String eventHash = EventHasher.eventHash(readTree(payloadJson), List.of());

        CultivationEvent event = CultivationEvent.builder()
                .batch(batch).seq(seq).eventType(EventType.THU_HOACH.code())
                .payloadJson(payloadJson).eventHash(eventHash)
                .status(EventStatus.CONFIRMED.name())
                .recordedBy(em.getReference(User.class, user.getId()))
                .recordedAt(recordedAt).occurredAt(request.harvestedAt())
                .clientEventId(request.clientEventId()).build();
        em.persist(event);
        enqueuePrivateOutbox(batch, seq, eventHash);

        batch.setHarvestedAt(request.harvestedAt());
        batch.setHarvestDate(Instant.ofEpochMilli(request.harvestedAt()).atZone(FARM_ZONE).toLocalDate());
        batch.setStatus(STATUS_HARVESTED);
        em.merge(batch);

        return Response.status(Response.Status.CREATED).entity(BaseResponse.build(toBatchView(batch))).build();
    }

    // ----------------------------------------------------------------------------- QC

    @POST
    @Path("/qc")
    @Consumes(MediaType.APPLICATION_JSON)
    @RequireRoles({"ADMIN", "ENGINEER"})
    public Response recordQc(@PathParam("batchId") Long batchId, QcRequest request) {
        UserSecurityDetailsPrincipal user = requireUser();
        if (request == null || request.brix() == null || request.avgWeightKg() == null) {
            throw new BadRequestException("brix and avgWeightKg are required");
        }
        if (request.brix() < 0 || request.brix() > 30) throw new BadRequestException("brix out of range [0,30]");
        if (request.avgWeightKg() < 0.1 || request.avgWeightKg() > 20) {
            throw new BadRequestException("avgWeightKg out of range [0.1,20]");
        }

        Batch batch = em.find(Batch.class, batchId, LockModeType.PESSIMISTIC_WRITE);
        if (batch == null) throw new NotFoundException();
        EventWriteGuard.requireBatchWritable(batch.getStatus());
        if (batch.getHarvestedAt() == null) {
            throw new EventWriteGuard.ConflictException("Batch must be harvested before QC");
        }

        Variety variety = batch.getVariety() == null ? null : em.find(Variety.class, batch.getVariety().getId());
        Double minBrix = variety == null ? null : variety.getMinBrix();
        Integer minDays = variety == null ? null : variety.getMinIsolationDays();
        if (minBrix == null || minDays == null) {
            // Missing variety configuration must never be treated as a pass (FR-QC rule).
            return problem(422, "MISSING_THRESHOLD",
                    "Variety thresholds (min Brix / isolation days) are not configured");
        }

        List<CultivationEvent> events = em.createQuery(
                        "select e from CultivationEvent e where e.batch.id = :batchId and e.isActive = true",
                        CultivationEvent.class)
                .setParameter("batchId", batchId).getResultList();
        IsolationPolicy.Result isolation = IsolationPolicy.evaluate(
                IsolationInputs.fromEvents(events), minDays, batch.getHarvestedAt());

        boolean brixOk = request.brix() >= minBrix;
        boolean isolationOk = isolation.outcome() == IsolationPolicy.Outcome.SUFFICIENT;
        boolean confirmed = Boolean.TRUE.equals(request.confirmPassed());

        // An engineer cannot override an unmet isolation window into a pass.
        if (confirmed && !isolationOk) {
            return problem(422, "ISOLATION_NOT_ENOUGH",
                    "Isolation window before harvest is not satisfied: " + isolation.outcome());
        }

        boolean passed = brixOk && isolationOk && confirmed;
        long now = System.currentTimeMillis();

        QcRecord record = QcRecord.builder()
                .batch(batch).brix(request.brix()).avgWeightKg(request.avgWeightKg())
                .passed(passed)
                .inspectedByUser(em.getReference(User.class, user.getId()))
                .inspectedAt(now)
                .sizeDescription(request.sizeDescription()).note(request.note())
                .thresholdBrixUsed(minBrix).minIsolationDaysUsed(minDays)
                .isolationEligibleAt(isolation.eligibleAt()).harvestedAtUsed(batch.getHarvestedAt())
                .status(passed ? STATUS_QC_PASSED : STATUS_REJECTED)
                .superseded(false)
                .build();
        em.persist(record);

        batch.setStatus(passed ? STATUS_QC_PASSED : STATUS_REJECTED);
        em.merge(batch);

        return Response.status(Response.Status.CREATED).entity(BaseResponse.build(toQcView(record))).build();
    }

    @GET
    @Path("/qc")
    public Response listQc(@PathParam("batchId") Long batchId) {
        Batch batch = em.find(Batch.class, batchId);
        if (batch == null) throw new NotFoundException();
        List<QcRecord> records = em.createQuery(
                        "select q from QcRecord q where q.batch.id = :batchId and q.isActive = true order by q.id desc",
                        QcRecord.class)
                .setParameter("batchId", batchId).getResultList();
        revalidateStalePasses(batch, records);
        return Response.ok(BaseResponse.build(records.stream().map(this::toQcView).toList())).build();
    }

    /**
     * Fail-safe invalidation at the point of use: recompute isolation from the current journal and
     * mark any still-active pass whose basis has changed as {@code superseded}, demoting the batch out
     * of QC_PASSED so a stale result can never flow to packaging. Runs in the GET transaction.
     */
    private void revalidateStalePasses(Batch batch, List<QcRecord> records) {
        if (batch.getHarvestedAt() == null) return;
        Variety variety = batch.getVariety() == null ? null : em.find(Variety.class, batch.getVariety().getId());
        Integer minDays = variety == null ? null : variety.getMinIsolationDays();
        List<CultivationEvent> events = em.createQuery(
                        "select e from CultivationEvent e where e.batch.id = :batchId and e.isActive = true",
                        CultivationEvent.class)
                .setParameter("batchId", batch.getId()).getResultList();
        IsolationPolicy.Result current = IsolationPolicy.evaluate(
                IsolationInputs.fromEvents(events), minDays, batch.getHarvestedAt());
        boolean demoted = false;
        for (QcRecord r : records) {
            boolean wasPassed = Boolean.TRUE.equals(r.getPassed()) && !Boolean.TRUE.equals(r.getSuperseded());
            if (QcStaleness.isStale(wasPassed, r.getIsolationEligibleAt(), current, batch.getHarvestedAt())) {
                r.setSuperseded(true);
                em.merge(r);
                demoted = true;
            }
        }
        if (demoted && STATUS_QC_PASSED.equalsIgnoreCase(batch.getStatus())) {
            // Basis changed under a passed batch: require re-inspection before packaging.
            batch.setStatus(STATUS_HARVESTED);
            em.merge(batch);
        }
    }

    // ----------------------------------------------------------------------------- helpers

    private UserSecurityDetailsPrincipal requireUser() {
        if (!(securityContext.getUserPrincipal() instanceof UserSecurityDetailsPrincipal user)) {
            throw new ForbiddenException();
        }
        return user;
    }

    private void requireAssignedOrEngineer(Long batchId) {
        if (securityContext.isUserInRole("ADMIN") || securityContext.isUserInRole("ENGINEER")) return;
        if (!(securityContext.getUserPrincipal() instanceof UserSecurityDetailsPrincipal user)) {
            throw new ForbiddenException();
        }
        Long assigned = em.createQuery(
                        "select count(a) from BatchAssignment a where a.batch.id = :batchId and a.user.id = :userId and a.isActive = true",
                        Long.class)
                .setParameter("batchId", batchId).setParameter("userId", user.getId()).getSingleResult();
        if (assigned == 0) throw new ForbiddenException();
    }

    private int nextSeq(Long batchId) {
        Integer maxSeq = em.createQuery(
                        "select max(e.seq) from CultivationEvent e where e.batch.id = :batchId", Integer.class)
                .setParameter("batchId", batchId).getSingleResult();
        return maxSeq == null ? 1 : maxSeq + 1;
    }

    private void enqueuePrivateOutbox(Batch batch, int seq, String eventHash) {
        OutboxEvent outbox = OutboxEvent.builder()
                .batchIdHash(EventHasher.batchIdHash(batch.getId(), batch.getSaltEnc()))
                .seq(seq).eventData(eventHash)
                .status(OutboxEvent.OutboxStatus.PENDING)
                .attempts(0).build();
        em.persist(outbox);
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

    private Response problem(int status, String code, String message) {
        var body = JSON.createObjectNode();
        body.put("type", "about:blank");
        body.put("status", status);
        body.put("code", code);
        body.put("title", message);
        return Response.status(status).type("application/problem+json").entity(body.toString()).build();
    }

    private BatchView toBatchView(Batch batch) {
        return new BatchView(batch.getId(), batch.getBatchCode(),
                batch.getVariety() == null ? null : batch.getVariety().getId(),
                batch.getStatus(), batch.getHarvestedAt(),
                batch.getHarvestDate() == null ? null : batch.getHarvestDate().toString());
    }

    private QcView toQcView(QcRecord r) {
        return new QcView(r.getId(), r.getBatch() == null ? null : r.getBatch().getId(),
                r.getBrix(), r.getAvgWeightKg(), r.getPassed(), r.getStatus(),
                r.getInspectedByUser() == null ? null : r.getInspectedByUser().getId(),
                r.getInspectedAt(), r.getSizeDescription(), r.getNote(),
                r.getThresholdBrixUsed(), r.getMinIsolationDaysUsed(),
                r.getIsolationEligibleAt(), r.getHarvestedAtUsed(), r.getSuperseded());
    }

    public record BatchView(Long id, String batchCode, Long varietyId, String status,
            Long harvestedAt, String harvestDate) {}

    public record QcView(Long id, Long batchId, Double brix, Double avgWeightKg, Boolean passed, String status,
            Long inspectedBy, Long inspectedAt, String sizeDescription, String note,
            Double thresholdBrixUsed, Integer minIsolationDaysUsed, Long isolationEligibleAt,
            Long harvestedAtUsed, Boolean superseded) {}
}
