package jakarta.ecofarm_backend.modules.quality;

import java.util.List;

/**
 * Request bodies for the harvest and quality-control endpoints.
 *
 * <p>The server never trusts a client-supplied pass verdict or isolation-day override: {@code
 * confirmPassed} is only the engineer's final sign-off on a result the server itself computed from
 * the variety thresholds. {@code harvestedAt} is the device-reported harvest instant in epoch ms.
 */
public final class QualityRequests {

    private QualityRequests() {}

    public record HarvestRequest(
            String clientEventId,
            Long harvestedAt,
            Integer estimatedQuantity,
            String area) {}

    public record QcRequest(
            Double brix,
            Double avgWeightKg,
            Boolean confirmPassed,
            String sizeDescription,
            String note,
            List<String> attachmentFileKeys) {}
}
