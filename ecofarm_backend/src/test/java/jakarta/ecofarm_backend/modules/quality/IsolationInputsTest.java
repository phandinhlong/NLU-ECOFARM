package jakarta.ecofarm_backend.modules.quality;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import java.util.List;
import org.junit.jupiter.api.Test;

class IsolationInputsTest {

    private static CultivationEvent event(String type, String status, Long occurredAt) {
        CultivationEvent e = new CultivationEvent();
        e.setEventType(type);
        e.setStatus(status);
        e.setOccurredAt(occurredAt);
        return e;
    }

    @Test
    void mapsIsolationRelevantTypesOnly() {
        assertEquals(IsolationPolicy.Activity.STOP_APPLICATION, IsolationInputs.activityOf("NGUNG_PHAN"));
        assertEquals(IsolationPolicy.Activity.FERTILIZE, IsolationInputs.activityOf("BON_PHAN"));
        assertEquals(IsolationPolicy.Activity.SPRAY_MICRONUTRIENTS, IsolationInputs.activityOf("PHUN_VI_LUONG"));
        assertEquals(IsolationPolicy.Activity.WATER, IsolationInputs.activityOf("TUOI_NUOC"));
        // Unrelated or unknown types are not part of the isolation window.
        assertNull(IsolationInputs.activityOf("GHI_CHU"));
        assertNull(IsolationInputs.activityOf("DO_BRIX"));
        assertNull(IsolationInputs.activityOf("THU_HOACH"));
        assertNull(IsolationInputs.activityOf("NOT_A_TYPE"));
    }

    @Test
    void mapsStatusToReviewAndTreatsUnknownAsPending() {
        assertEquals(IsolationPolicy.Review.APPROVED, IsolationInputs.reviewOf("APPROVED"));
        assertEquals(IsolationPolicy.Review.REJECTED, IsolationInputs.reviewOf("REJECTED"));
        assertEquals(IsolationPolicy.Review.SUPERSEDED, IsolationInputs.reviewOf("SUPERSEDED"));
        assertEquals(IsolationPolicy.Review.PENDING, IsolationInputs.reviewOf("PENDING"));
        assertEquals(IsolationPolicy.Review.PENDING, IsolationInputs.reviewOf("CONFIRMED"));
        assertEquals(IsolationPolicy.Review.PENDING, IsolationInputs.reviewOf(null));
        assertEquals(IsolationPolicy.Review.PENDING, IsolationInputs.reviewOf("garbage"));
    }

    @Test
    void dropsUnrelatedTypesAndNullTimesWhenBuildingFromEvents() {
        var events = List.of(
                event("NGUNG_PHAN", "APPROVED", 1_000L),
                event("GHI_CHU", "APPROVED", 2_000L),       // irrelevant type -> dropped
                event("BON_PHAN", "APPROVED", null),         // no occurredAt -> dropped
                event("TUOI_NUOC", "PENDING", 3_000L));
        var mapped = IsolationInputs.fromEvents(events);
        assertEquals(2, mapped.size());
        assertTrue(mapped.stream().anyMatch(e -> e.activity() == IsolationPolicy.Activity.STOP_APPLICATION));
        assertTrue(mapped.stream().anyMatch(e -> e.activity() == IsolationPolicy.Activity.WATER));
    }

    @Test
    void nullListYieldsEmpty() {
        assertTrue(IsolationInputs.fromEvents(null).isEmpty());
    }
}
