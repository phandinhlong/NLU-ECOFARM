package jakarta.ecofarm_backend.modules.quality;

/**
 * Pure check for whether a stored QC pass has gone stale because the isolation basis changed after
 * it was computed.
 *
 * <p>A QC result records the exact basis it was decided on ({@code harvestedAtUsed}, the threshold
 * and the derived {@code isolationEligibleAt}). Every consumption point (reading QC, re-running QC,
 * the packaging gate) recomputes isolation from the current journal and calls this: if the recomputed
 * eligibility no longer matches what the pass was based on — or the window is no longer satisfied —
 * the pass is stale and must not be honoured. This keeps invalidation fail-safe at the point of use
 * instead of hooking every journal mutation.
 */
public final class QcStaleness {

    private QcStaleness() {}

    /**
     * @param wasPassed        whether the stored record is a pass
     * @param storedEligibleAt the isolation_eligible_at stored with the pass (may be null)
     * @param current          freshly recomputed isolation result against the current journal
     * @param harvestedAt      the harvest instant used to recompute
     * @return true when a previously-passing record can no longer be trusted
     */
    public static boolean isStale(boolean wasPassed, Long storedEligibleAt,
            IsolationPolicy.Result current, long harvestedAt) {
        if (!wasPassed) return false;
        // The window must still be satisfied against the current journal.
        if (current.outcome() != IsolationPolicy.Outcome.SUFFICIENT) return true;
        // And the earliest-eligible instant must be unchanged; a new/edited stop moves it.
        if (storedEligibleAt == null || current.eligibleAt() == null) return true;
        if (!storedEligibleAt.equals(current.eligibleAt())) return true;
        // Harvest must still be at/after eligibility (defensive; SUFFICIENT already implies it).
        return harvestedAt < current.eligibleAt();
    }
}
