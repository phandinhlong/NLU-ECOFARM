package jakarta.ecofarm_backend.modules.log;

/**
 * Computes the device/server clock difference for an event and decides whether it needs review.
 *
 * <p>Offline writes carry the device's wall-clock at submit time; comparing it to the server's
 * receive time surfaces a device whose clock drifted far enough that its timestamps are suspect.
 * The server never rewrites {@code occurredAt} or the hashed payload from this — it only records the
 * skew and raises a review flag past a configurable threshold.
 */
public final class ClockSkew {

    private ClockSkew() {
    }

    /** Signed skew in ms, or null when the device did not report its send time. */
    public static Long skewMs(Long deviceSentAt, long serverReceivedAt) {
        return deviceSentAt == null ? null : serverReceivedAt - deviceSentAt;
    }

    public static boolean needsReview(Long skewMs, long thresholdMs) {
        return skewMs != null && Math.abs(skewMs) > thresholdMs;
    }
}
