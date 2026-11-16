package jakarta.ecofarm_backend.modules.log;

/**
 * Lifecycle status of a cultivation event, stored as a string in {@code cultivation_events.status}
 * to match the existing read path.
 *
 * <p>Chain confirmation and approval are tracked independently: an event waits in {@link #PENDING}
 * until the private ledger confirms it ({@link #CONFIRMED}), and only a confirmed event can be
 * {@link #APPROVED} or {@link #REJECTED}. An amendment moves the prior event to {@link #SUPERSEDED}
 * without touching its payload or hash. A ledger callback must never overwrite a terminal review
 * status, so these are kept separate from the chain-confirmation flag on the outbox.
 */
public enum EventStatus {
    PENDING,
    CONFIRMED,
    APPROVED,
    REJECTED,
    SUPERSEDED;

    public boolean isTerminalReview() {
        return this == APPROVED || this == REJECTED || this == SUPERSEDED;
    }
}
