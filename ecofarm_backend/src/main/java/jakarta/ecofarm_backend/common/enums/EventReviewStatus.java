package jakarta.ecofarm_backend.common.enums;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public enum EventReviewStatus {
    UNREVIEWED,
    APPROVED,
    REJECTED;

    private static final Map<EventReviewStatus, Map<ReviewAction, EventReviewStatus>> TRANSITIONS;

    static {
        Map<EventReviewStatus, Map<ReviewAction, EventReviewStatus>> map = new EnumMap<>(EventReviewStatus.class);
        map.put(UNREVIEWED, transitions(
                ReviewAction.APPROVE, APPROVED,
                ReviewAction.REJECT, REJECTED));
        map.put(APPROVED, transitions());
        map.put(REJECTED, transitions());
        TRANSITIONS = Collections.unmodifiableMap(map);
    }

    private static Map<ReviewAction, EventReviewStatus> transitions(Object... pairs) {
        Map<ReviewAction, EventReviewStatus> map = new EnumMap<>(ReviewAction.class);
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((ReviewAction) pairs[i], (EventReviewStatus) pairs[i + 1]);
        }
        return Collections.unmodifiableMap(map);
    }

    public EventReviewStatus fire(ReviewAction action) {
        Map<ReviewAction, EventReviewStatus> row = TRANSITIONS.get(this);
        EventReviewStatus target = row != null ? row.get(action) : null;
        if (target == null) {
            throw new IllegalStateException("Invalid transition from " + this + " with action " + action);
        }
        return target;
    }

    public boolean canFire(ReviewAction action) {
        Map<ReviewAction, EventReviewStatus> row = TRANSITIONS.get(this);
        return row != null && row.containsKey(action);
    }
}
