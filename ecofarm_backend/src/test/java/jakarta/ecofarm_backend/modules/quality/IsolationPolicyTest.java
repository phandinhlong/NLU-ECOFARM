package jakarta.ecofarm_backend.modules.quality;

import static jakarta.ecofarm_backend.modules.quality.IsolationPolicy.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class IsolationPolicyTest {
    private static final long DAY = 86_400_000L;

    @Test
    void requiresTheFullIsolationPeriod() {
        var events = List.of(new Event(Activity.STOP_APPLICATION, Review.APPROVED, DAY));
        assertEquals(new Result(Outcome.TOO_EARLY, 15 * DAY), IsolationPolicy.evaluate(events, 14, 15 * DAY - 1));
        assertEquals(new Result(Outcome.SUFFICIENT, 15 * DAY), IsolationPolicy.evaluate(events, 14, 15 * DAY));
    }

    @Test
    void oneMillisecondShortIsStillTooEarly() {
        var events = List.of(new Event(Activity.STOP_APPLICATION, Review.APPROVED, 0L));
        assertEquals(Outcome.TOO_EARLY, IsolationPolicy.evaluate(events, 14, 14 * DAY - 1).outcome());
        assertEquals(Outcome.SUFFICIENT, IsolationPolicy.evaluate(events, 14, 14 * DAY).outcome());
    }

    @Test
    void applicationAfterStoppingRequiresAnotherStop() {
        var events = List.of(new Event(Activity.STOP_APPLICATION, Review.APPROVED, DAY), new Event(Activity.FERTILIZE, Review.APPROVED, 2 * DAY));
        assertEquals(Outcome.NEW_STOP_REQUIRED, IsolationPolicy.evaluate(events, 14, 20 * DAY).outcome());
        var corrected = List.of(events.get(0), events.get(1), new Event(Activity.STOP_APPLICATION, Review.APPROVED, 3 * DAY));
        assertEquals(new Result(Outcome.SUFFICIENT, 17 * DAY), IsolationPolicy.evaluate(corrected, 14, 20 * DAY));
    }

    @Test
    void ignoresWaterAndRejectedOrSupersededApplications() {
        var events = List.of(new Event(Activity.STOP_APPLICATION, Review.APPROVED, DAY), new Event(Activity.WATER, Review.APPROVED, 2 * DAY),
                new Event(Activity.FERTILIZE, Review.REJECTED, 3 * DAY), new Event(Activity.SPRAY_MICRONUTRIENTS, Review.SUPERSEDED, 4 * DAY));
        assertEquals(Outcome.SUFFICIENT, IsolationPolicy.evaluate(events, 14, 15 * DAY).outcome());
    }

    @Test
    void pendingWaterDoesNotBlockButPendingApplicationDoes() {
        var stop = new Event(Activity.STOP_APPLICATION, Review.APPROVED, DAY);
        var pendingWater = new Event(Activity.WATER, Review.PENDING, 2 * DAY);
        assertEquals(Outcome.SUFFICIENT, IsolationPolicy.evaluate(List.of(stop, pendingWater), 14, 20 * DAY).outcome());
        var pendingSpray = new Event(Activity.SPRAY_MICRONUTRIENTS, Review.PENDING, 2 * DAY);
        assertEquals(Outcome.PENDING_REVIEW, IsolationPolicy.evaluate(List.of(stop, pendingSpray), 14, 20 * DAY).outcome());
    }

    @Test
    void blocksUnreviewedApplicationsAndMissingInformation() {
        var stop = new Event(Activity.STOP_APPLICATION, Review.APPROVED, DAY);
        var pending = new Event(Activity.SPRAY_MICRONUTRIENTS, Review.PENDING, 2 * DAY);
        assertEquals(Outcome.PENDING_REVIEW, IsolationPolicy.evaluate(List.of(stop, pending), 14, 20 * DAY).outcome());
        assertEquals(Outcome.MISSING_THRESHOLD, IsolationPolicy.evaluate(List.of(stop), null, 20 * DAY).outcome());
        assertEquals(Outcome.MISSING_THRESHOLD, IsolationPolicy.evaluate(List.of(stop), -1, 20 * DAY).outcome());
        assertEquals(Outcome.MISSING_STOP, IsolationPolicy.evaluate(List.of(), 14, 20 * DAY).outcome());
    }

    @Test
    void eventsAfterHarvestAreIgnored() {
        var stop = new Event(Activity.STOP_APPLICATION, Review.APPROVED, DAY);
        var lateFertilize = new Event(Activity.FERTILIZE, Review.APPROVED, 100 * DAY);
        // The late application is after harvest, so it must not force a new stop.
        assertEquals(Outcome.SUFFICIENT, IsolationPolicy.evaluate(List.of(stop, lateFertilize), 14, 20 * DAY).outcome());
    }
}
