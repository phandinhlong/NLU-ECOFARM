package jakarta.ecofarm_backend.modules.quality;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class QcStalenessTest {
    private static final long DAY = 86_400_000L;

    private static IsolationPolicy.Result sufficient(long eligibleAt) {
        return new IsolationPolicy.Result(IsolationPolicy.Outcome.SUFFICIENT, eligibleAt);
    }

    @Test
    void nonPassIsNeverStale() {
        assertFalse(QcStaleness.isStale(false, 15 * DAY, sufficient(15 * DAY), 20 * DAY));
    }

    @Test
    void passWithUnchangedBasisIsFresh() {
        assertFalse(QcStaleness.isStale(true, 15 * DAY, sufficient(15 * DAY), 20 * DAY));
    }

    @Test
    void passBecomesStaleWhenEligibilityMoves() {
        // An added/edited stop pushed the earliest-eligible instant later than what the pass used.
        assertTrue(QcStaleness.isStale(true, 15 * DAY, sufficient(17 * DAY), 20 * DAY));
    }

    @Test
    void passBecomesStaleWhenWindowNoLongerSatisfied() {
        var blocked = new IsolationPolicy.Result(IsolationPolicy.Outcome.NEW_STOP_REQUIRED, null);
        assertTrue(QcStaleness.isStale(true, 15 * DAY, blocked, 20 * DAY));
        var pending = new IsolationPolicy.Result(IsolationPolicy.Outcome.PENDING_REVIEW, null);
        assertTrue(QcStaleness.isStale(true, 15 * DAY, pending, 20 * DAY));
    }

    @Test
    void passWithMissingStoredBasisIsTreatedAsStale() {
        assertTrue(QcStaleness.isStale(true, null, sufficient(15 * DAY), 20 * DAY));
    }
}
