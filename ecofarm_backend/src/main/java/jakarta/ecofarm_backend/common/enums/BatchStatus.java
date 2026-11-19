package jakarta.ecofarm_backend.common.enums;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public enum BatchStatus {
    DRAFT,
    GROWING,
    QC_PASSED,
    HARVESTED,
    REJECTED,
    ANCHORING(true),
    ANCHOR_FAILED(true),
    ACTIVE(true),
    SHIPPED(true),
    RECALLED(true);

    private final boolean locked;
    private static final Map<BatchStatus, Map<BatchEvent, BatchStatus>> TRANSITIONS;

    BatchStatus() {
        this(false);
    }

    BatchStatus(boolean locked) {
        this.locked = locked;
    }

    public boolean isLocked() {
        return locked;
    }

    static {
        Map<BatchStatus, Map<BatchEvent, BatchStatus>> map = new EnumMap<>(BatchStatus.class);
        map.put(DRAFT, transitions(BatchEvent.START_GROWING, GROWING));
        map.put(GROWING, transitions(
                BatchEvent.PASS_QC, QC_PASSED,
                BatchEvent.REJECT, REJECTED));
        map.put(QC_PASSED, transitions(
                BatchEvent.HARVEST, HARVESTED,
                BatchEvent.REJECT, REJECTED));
        map.put(HARVESTED, transitions(
                BatchEvent.START_ANCHORING, ANCHORING,
                BatchEvent.REJECT, REJECTED));
        map.put(ANCHORING, transitions(
                BatchEvent.ANCHOR_CONFIRMED, ACTIVE,
                BatchEvent.ANCHOR_FAILED, ANCHOR_FAILED));
        map.put(ANCHOR_FAILED, transitions(BatchEvent.RETRY_ANCHORING, ANCHORING));
        map.put(ACTIVE, transitions(
                BatchEvent.SHIP, SHIPPED,
                BatchEvent.RECALL, RECALLED));
        map.put(SHIPPED, transitions(BatchEvent.RECALL, RECALLED));
        map.put(REJECTED, transitions());
        map.put(RECALLED, transitions());
        TRANSITIONS = Collections.unmodifiableMap(map);
    }

    private static Map<BatchEvent, BatchStatus> transitions(Object... pairs) {
        Map<BatchEvent, BatchStatus> map = new EnumMap<>(BatchEvent.class);
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((BatchEvent) pairs[i], (BatchStatus) pairs[i + 1]);
        }
        return Collections.unmodifiableMap(map);
    }

    public BatchStatus fire(BatchEvent event) {
        BatchStatus target = TRANSITIONS.get(this).get(event);
        if (target == null) {
            throw new IllegalStateException("Invalid transition from " + this + " with event " + event);
        }
        return target;
    }

    public boolean canFire(BatchEvent event) {
        return TRANSITIONS.get(this).containsKey(event);
    }
}