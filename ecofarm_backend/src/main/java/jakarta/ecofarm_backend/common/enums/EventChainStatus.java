package jakarta.ecofarm_backend.common.enums;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public enum EventChainStatus {
    PENDING,
    CONFIRMED,
    SUPERSEDED;

    private static final Map<EventChainStatus, Map<ChainAction, EventChainStatus>> TRANSITIONS;

    static {
        Map<EventChainStatus, Map<ChainAction, EventChainStatus>> map = new EnumMap<>(EventChainStatus.class);
        map.put(PENDING, transitions(
                ChainAction.CONFIRM, CONFIRMED,
                ChainAction.SUPERSEDE, SUPERSEDED));
        map.put(CONFIRMED, transitions(ChainAction.SUPERSEDE, SUPERSEDED));
        map.put(SUPERSEDED, transitions());
        TRANSITIONS = Collections.unmodifiableMap(map);
    }

    private static Map<ChainAction, EventChainStatus> transitions(Object... pairs) {
        Map<ChainAction, EventChainStatus> map = new EnumMap<>(ChainAction.class);
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((ChainAction) pairs[i], (EventChainStatus) pairs[i + 1]);
        }
        return Collections.unmodifiableMap(map);
    }

    public EventChainStatus fire(ChainAction action) {
        Map<ChainAction, EventChainStatus> row = TRANSITIONS.get(this);
        EventChainStatus target = row != null ? row.get(action) : null;
        if (target == null) {
            throw new IllegalStateException("Invalid transition from " + this + " with action " + action);
        }
        return target;
    }

    public boolean canFire(ChainAction action) {
        Map<ChainAction, EventChainStatus> row = TRANSITIONS.get(this);
        return row != null && row.containsKey(action);
    }
}
