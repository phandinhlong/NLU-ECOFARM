package jakarta.ecofarm_backend.common.enums;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public enum AnchorStatus {
    PENDING,
    SUBMITTED,
    CONFIRMED,
    FAILED;

    private static final Map<AnchorStatus, Map<AnchorEvent, AnchorStatus>> TRANSITIONS;

    static {
        Map<AnchorStatus, Map<AnchorEvent, AnchorStatus>> map = new EnumMap<>(AnchorStatus.class);
        map.put(PENDING, transitions(
                AnchorEvent.SUBMIT, SUBMITTED,
                AnchorEvent.FAIL, FAILED));
        map.put(SUBMITTED, transitions(
                AnchorEvent.CONFIRM, CONFIRMED,
                AnchorEvent.FAIL, FAILED));
        map.put(FAILED, transitions(
                AnchorEvent.RETRY, PENDING));
        map.put(CONFIRMED, transitions());
        TRANSITIONS = Collections.unmodifiableMap(map);
    }

    private static Map<AnchorEvent, AnchorStatus> transitions(Object... pairs) {
        Map<AnchorEvent, AnchorStatus> map = new EnumMap<>(AnchorEvent.class);
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((AnchorEvent) pairs[i], (AnchorStatus) pairs[i + 1]);
        }
        return Collections.unmodifiableMap(map);
    }

    public AnchorStatus fire(AnchorEvent event) {
        Map<AnchorEvent, AnchorStatus> row = TRANSITIONS.get(this);
        AnchorStatus target = row != null ? row.get(event) : null;
        if (target == null) {
            throw new IllegalStateException("Invalid transition from " + this + " with event " + event);
        }
        return target;
    }

    public boolean canFire(AnchorEvent event) {
        Map<AnchorEvent, AnchorStatus> row = TRANSITIONS.get(this);
        return row != null && row.containsKey(event);
    }

    public static AnchorStatus fromString(String status) {
        if (status == null || status.isBlank()) {
            return CONFIRMED;
        }
        try {
            return AnchorStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return CONFIRMED;
        }
    }

    public BatchEvent toBatchEvent() {
        switch (this) {
            case CONFIRMED:
                return BatchEvent.ANCHOR_CONFIRMED;
            case FAILED:
                return BatchEvent.ANCHOR_FAILED;
            default:
                return null;
        }
    }

    public BatchStatus toBatchStatus() {
        BatchStatus current = BatchStatus.ANCHORING;
        BatchEvent event = toBatchEvent();
        return (event != null && current.canFire(event)) ? current.fire(event) : current;
    }
}
