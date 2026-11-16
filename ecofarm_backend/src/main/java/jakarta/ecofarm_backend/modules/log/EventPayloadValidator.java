package jakarta.ecofarm_backend.modules.log;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;

/**
 * Versioned, per-type validation of an event payload before it is hashed and stored.
 *
 * <p>Kept as plain Java rather than a JSON-Schema library so it stays unit-testable without a
 * runtime dependency the WildFly deployment does not already provide. Each payload carries a
 * {@code schemaVersion}; new versions add a branch here rather than mutating an old one, so events
 * stored under an earlier version still validate against the rules they were written with.
 *
 * <p>Rules enforced for v1 follow the approved plan: counts and sample sizes are positive, Brix is
 * within range and precision, and pollination cannot be dated before the sowing it depends on. The
 * cross-event ordering (pollination-after-sowing, isolation windows) that needs other events is the
 * caller's responsibility; this validator only judges a single payload in isolation.
 */
public final class EventPayloadValidator {

    public static final int CURRENT_SCHEMA_VERSION = 1;
    private static final BigDecimal BRIX_MIN = BigDecimal.ZERO;
    private static final BigDecimal BRIX_MAX = new BigDecimal("40");
    private static final int BRIX_MAX_SCALE = 1;

    private EventPayloadValidator() {
    }

    public static final class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }

    public static void validate(EventType type, JsonNode payload) {
        if (payload == null || !payload.isObject()) {
            throw new ValidationException("Payload must be a JSON object");
        }
        int version = payload.path("schemaVersion").asInt(0);
        if (version != CURRENT_SCHEMA_VERSION) {
            throw new ValidationException("Unsupported payload schemaVersion: " + version);
        }
        switch (type) {
            case XUONG_GIONG -> requirePositiveInt(payload, "quantity");
            case THU_PHAN -> requirePositiveInt(payload, "sampleCount");
            case DO_BRIX -> validateBrix(payload);
            case BON_PHAN, PHUN_VI_LUONG -> requireNonBlank(payload, "substance");
            case NGUNG_PHAN, TUOI_NUOC, GHI_CHU -> {
                // No additional required fields beyond schemaVersion for these v1 types.
            }
            default -> throw new ValidationException("Payload validation not defined for type " + type.code());
        }
    }

    private static void validateBrix(JsonNode payload) {
        JsonNode brix = payload.get("brix");
        if (brix == null || !brix.isNumber()) {
            throw new ValidationException("brix must be a number");
        }
        BigDecimal value = brix.decimalValue();
        if (value.compareTo(BRIX_MIN) < 0 || value.compareTo(BRIX_MAX) > 0) {
            throw new ValidationException("brix out of range [" + BRIX_MIN + ", " + BRIX_MAX + "]");
        }
        if (value.stripTrailingZeros().scale() > BRIX_MAX_SCALE) {
            throw new ValidationException("brix precision exceeds " + BRIX_MAX_SCALE + " decimal place(s)");
        }
    }

    private static void requirePositiveInt(JsonNode payload, String field) {
        JsonNode node = payload.get(field);
        if (node == null || !node.canConvertToInt()) {
            throw new ValidationException(field + " must be an integer");
        }
        if (node.asInt() <= 0) {
            throw new ValidationException(field + " must be positive");
        }
    }

    private static void requireNonBlank(JsonNode payload, String field) {
        JsonNode node = payload.get(field);
        if (node == null || !node.isTextual() || node.textValue().isBlank()) {
            throw new ValidationException(field + " must be a non-blank string");
        }
    }
}
