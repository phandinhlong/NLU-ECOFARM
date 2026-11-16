package jakarta.ecofarm_backend.common.enums;

import java.util.Set;

public enum BatchStatus {
    DRAFT,
    GROWING,
    HARVESTED,
    QC_PASSED,
    REJECTED,
    ANCHORING(true),
    ANCHOR_FAILED(true),
    ACTIVE(true),
    SHIPPED(true),
    RECALLED(true);

    private final boolean locked;

    BatchStatus() {
        this(false);
    }

    BatchStatus(boolean locked) {
        this.locked = locked;
    }

    public boolean isLocked() {
        return locked;
    }

    public Set<BatchStatus> next() {
        return switch (this) {
            case DRAFT -> Set.of(GROWING);
            case GROWING -> Set.of(HARVESTED);
            case HARVESTED -> Set.of(QC_PASSED, REJECTED);
            case QC_PASSED -> Set.of(ANCHORING);
            case ANCHORING -> Set.of(ACTIVE, ANCHOR_FAILED);
            case ANCHOR_FAILED -> Set.of(ANCHORING);
            case ACTIVE -> Set.of(SHIPPED, RECALLED);
            case SHIPPED -> Set.of(RECALLED);
            case REJECTED, RECALLED -> Set.of();
        };
    }

    public boolean canTransitionTo(BatchStatus target) {
        return target != null && next().contains(target);
    }
}