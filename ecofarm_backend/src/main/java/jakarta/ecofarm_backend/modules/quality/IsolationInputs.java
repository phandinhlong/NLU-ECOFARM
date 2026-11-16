package jakarta.ecofarm_backend.modules.quality;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.modules.log.EventStatus;
import jakarta.ecofarm_backend.modules.log.EventType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adapter from stored {@link CultivationEvent}s to the pure {@link IsolationPolicy} model.
 *
 * <p>Only the event types that affect the isolation window are mapped; anything else (sowing, Brix
 * reading, note, harvest, approval) is irrelevant to the window and dropped. The event time used is
 * {@code occurredAt} — the device-reported event instant — never the record or QC time, matching the
 * plan's rule that isolation is computed from the real event time. The wire status string is mapped
 * to the policy's review enum; an unknown or null status is treated as PENDING so it cannot silently
 * count as approved.
 */
public final class IsolationInputs {

    private IsolationInputs() {}

    public static List<IsolationPolicy.Event> fromEvents(List<CultivationEvent> events) {
        List<IsolationPolicy.Event> out = new ArrayList<>();
        if (events == null) return out;
        for (CultivationEvent e : events) {
            IsolationPolicy.Activity activity = activityOf(e.getEventType());
            if (activity == null) continue;
            Long occurredAt = e.getOccurredAt();
            if (occurredAt == null) continue;
            out.add(new IsolationPolicy.Event(activity, reviewOf(e.getStatus()), occurredAt));
        }
        return out;
    }

    static IsolationPolicy.Activity activityOf(String eventTypeCode) {
        Optional<EventType> type = EventType.fromCode(eventTypeCode);
        if (type.isEmpty()) return null;
        return switch (type.get()) {
            case NGUNG_PHAN -> IsolationPolicy.Activity.STOP_APPLICATION;
            case BON_PHAN -> IsolationPolicy.Activity.FERTILIZE;
            case PHUN_VI_LUONG -> IsolationPolicy.Activity.SPRAY_MICRONUTRIENTS;
            case TUOI_NUOC -> IsolationPolicy.Activity.WATER;
            default -> null;
        };
    }

    static IsolationPolicy.Review reviewOf(String statusCode) {
        if (statusCode == null) return IsolationPolicy.Review.PENDING;
        EventStatus status;
        try {
            status = EventStatus.valueOf(statusCode.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return IsolationPolicy.Review.PENDING;
        }
        return switch (status) {
            case APPROVED -> IsolationPolicy.Review.APPROVED;
            case REJECTED -> IsolationPolicy.Review.REJECTED;
            case SUPERSEDED -> IsolationPolicy.Review.SUPERSEDED;
            case PENDING, CONFIRMED -> IsolationPolicy.Review.PENDING;
        };
    }
}
