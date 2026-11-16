package jakarta.ecofarm_backend.modules.log;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Request bodies for the journal write endpoints, kept separate from the JPA entity so the API never
 * accepts or leaks entity relations, server-side ids or storage paths.
 *
 * <p>{@code occurredAt} is the device-reported event time in epoch milliseconds; the server sets
 * {@code recordedAt} itself and never trusts a client value for it. {@code clientEventId} carries the
 * idempotency key so a retry of the same event is recognized rather than duplicated.
 */
public final class EventRequests {

    private EventRequests() {
    }

    public record CreateEventRequest(
            String eventType,
            Long occurredAt,
            String clientEventId,
            JsonNode payload,
            java.util.List<String> attachmentFileKeys,
            // Device wall-clock at submit time (epoch ms). Lets the server flag a skewed clock
            // without trusting the device to set occurredAt/recordedAt. Optional for online writes.
            Long deviceSentAt) {
    }

    public record RejectRequest(String reason) {
    }

    public record AmendRequest(
            String clientEventId,
            Long occurredAt,
            JsonNode payload,
            String reason,
            java.util.List<String> attachmentFileKeys) {
    }
}
