package jakarta.ecofarm_backend.modules.log;

/**
 * Decides which ledger an event belongs to, so the create/approve paths enqueue outbox rows with the
 * correct destination instead of the current behaviour of sending every event to both Besu and
 * Polygon.
 *
 * <p>All LOG events and the system {@code EVENT_APPROVED} are private: they go to the Besu private
 * chain only and must never reach Polygon. A Besu failure retries; it never falls back to the public
 * chain. Public anchoring is a separate, explicitly-requested destination owned by LDG/PCK, out of
 * scope for commit 1. This is a pure classifier so the routing rule can be tested without a chain
 * client; the actual send is wired in the blockchain module against the frozen LDG contract.
 */
public final class OutboxRouter {

    public enum Destination { PRIVATE_ONLY, PUBLIC_ANCHOR }

    private OutboxRouter() {
    }

    public static Destination route(EventType type) {
        // Every client event and the system approval event are private. No LOG path targets Polygon.
        return Destination.PRIVATE_ONLY;
    }

    public static boolean reachesPublicChain(EventType type) {
        return route(type) == Destination.PUBLIC_ANCHOR;
    }
}
