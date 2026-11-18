package jakarta.ecofarm_backend.common.enums;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public enum TaskStatus {
    ASSIGNED,
    COMPLETED,
    CANCELLED;

    private static final Map<TaskStatus, Map<TaskAction, TaskStatus>> TRANSITIONS;

    static {
        Map<TaskStatus, Map<TaskAction, TaskStatus>> map = new EnumMap<>(TaskStatus.class);
        map.put(ASSIGNED, transitions(
                TaskAction.COMPLETE, COMPLETED,
                TaskAction.CANCEL, CANCELLED));
        map.put(COMPLETED, transitions());
        map.put(CANCELLED, transitions());
        TRANSITIONS = Collections.unmodifiableMap(map);
    }

    private static Map<TaskAction, TaskStatus> transitions(Object... pairs) {
        Map<TaskAction, TaskStatus> map = new EnumMap<>(TaskAction.class);
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((TaskAction) pairs[i], (TaskStatus) pairs[i + 1]);
        }
        return Collections.unmodifiableMap(map);
    }

    public TaskStatus fire(TaskAction action) {
        Map<TaskAction, TaskStatus> row = TRANSITIONS.get(this);
        TaskStatus target = row != null ? row.get(action) : null;
        if (target == null) {
            throw new IllegalStateException("Invalid transition from " + this + " with action " + action);
        }
        return target;
    }

    public boolean canFire(TaskAction action) {
        Map<TaskAction, TaskStatus> row = TRANSITIONS.get(this);
        return row != null && row.containsKey(action);
    }
}
