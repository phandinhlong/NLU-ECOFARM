package jakarta.ecofarm_backend.modules.quality;

import java.time.Duration;
import java.util.List;

/**
 * Pure isolation-period decision for a cultivation batch.
 *
 * <p>Kept free of persistence and container concerns so it stays unit-testable. The caller loads the
 * approved/pending events and the variety threshold; this class only answers whether the pre-harvest
 * isolation window is satisfied and, if so, the earliest eligible instant. A missing threshold is
 * reported, never defaulted to "passed".
 */
public final class IsolationPolicy {
    private static final long DAY_MILLIS = Duration.ofDays(1).toMillis();

    private IsolationPolicy() {}

    public enum Activity { STOP_APPLICATION, FERTILIZE, SPRAY_MICRONUTRIENTS, WATER }
    public enum Review { APPROVED, PENDING, REJECTED, SUPERSEDED }
    public enum Outcome { SUFFICIENT, MISSING_THRESHOLD, PENDING_REVIEW, MISSING_STOP, NEW_STOP_REQUIRED, TOO_EARLY }

    public record Event(Activity activity, Review review, long occurredAt) {}
    public record Result(Outcome outcome, Long eligibleAt) {}

    public static Result evaluate(List<Event> events, Integer minimumDays, long harvestAt) {
        if (minimumDays == null || minimumDays < 0) return new Result(Outcome.MISSING_THRESHOLD, null);
        long stopAt = Long.MIN_VALUE;
        long applicationAt = Long.MIN_VALUE;
        for (Event event : events) {
            if (event.occurredAt() > harvestAt || event.review() == Review.REJECTED || event.review() == Review.SUPERSEDED) continue;
            if (event.review() == Review.PENDING && event.activity() != Activity.WATER) return new Result(Outcome.PENDING_REVIEW, null);
            if (event.review() != Review.APPROVED) continue;
            if (event.activity() == Activity.STOP_APPLICATION) stopAt = Math.max(stopAt, event.occurredAt());
            if (event.activity() == Activity.FERTILIZE || event.activity() == Activity.SPRAY_MICRONUTRIENTS) applicationAt = Math.max(applicationAt, event.occurredAt());
        }
        if (stopAt == Long.MIN_VALUE) return new Result(Outcome.MISSING_STOP, null);
        if (applicationAt >= stopAt) return new Result(Outcome.NEW_STOP_REQUIRED, null);
        long eligibleAt = Math.addExact(stopAt, Math.multiplyExact((long) minimumDays, DAY_MILLIS));
        return new Result(harvestAt >= eligibleAt ? Outcome.SUFFICIENT : Outcome.TOO_EARLY, eligibleAt);
    }
}
