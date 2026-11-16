package jakarta.ecofarm_backend.modules.log;

import java.util.Set;

/**
 * Pure decision rules for writing to a batch journal, kept out of the resource so they can be tested
 * without a database or container.
 *
 * <p>The rules enforced here are the ones the plan makes load-bearing: once a batch enters an
 * anchoring/locked state no new write, approval, rejection or amendment is allowed; an event can
 * only be approved or rejected after the chain confirms it and while it is still pending review; and
 * an amendment may only target a confirmed, non-superseded event. The caller owns persistence,
 * row-locking and permission lookup; this class owns the "may I?" answers.
 */
public final class EventWriteGuard {

    /** Batch states that freeze the journal. Compared case-insensitively against {@code Batch.status}. */
    public static final Set<String> LOCKED_BATCH_STATES = Set.of("ANCHORING", "LOCKED", "ARCHIVED");

    private EventWriteGuard() {
    }

    public static final class ConflictException extends RuntimeException {
        public ConflictException(String message) {
            super(message);
        }
    }

    public static boolean isBatchLocked(String batchStatus) {
        return batchStatus != null && LOCKED_BATCH_STATES.contains(batchStatus.trim().toUpperCase());
    }

    public static void requireBatchWritable(String batchStatus) {
        if (isBatchLocked(batchStatus)) {
            throw new ConflictException("Batch is locked for writes in state: " + batchStatus);
        }
    }

    public static void requireApprovable(EventStatus current) {
        if (current != EventStatus.CONFIRMED) {
            throw new ConflictException("Only a chain-confirmed event can be approved or rejected; was " + current);
        }
    }

    public static void requireAmendable(EventStatus current) {
        if (current == EventStatus.SUPERSEDED) {
            throw new ConflictException("A superseded event cannot be amended again");
        }
        if (current == EventStatus.PENDING) {
            throw new ConflictException("An event must be chain-confirmed before it can be amended");
        }
    }

    /**
     * Idempotency verdict for a retried {@code clientEventId}. A retry that matches the stored event
     * returns the existing one (200); a reused id with different content is a conflict (409).
     */
    public static void requireRetryMatches(boolean sameOwner, boolean sameEventHash) {
        if (!sameOwner || !sameEventHash) {
            throw new ConflictException("clientEventId reused with different owner or content");
        }
    }
}
