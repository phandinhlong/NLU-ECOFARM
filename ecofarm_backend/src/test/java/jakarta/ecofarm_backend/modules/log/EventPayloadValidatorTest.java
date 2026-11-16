package jakarta.ecofarm_backend.modules.log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventPayloadValidatorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private JsonNode json(String s) throws Exception {
        return mapper.readTree(s);
    }

    @Test
    void acceptsValidBrixWithinRangeAndPrecision() throws Exception {
        assertDoesNotThrow(() -> EventPayloadValidator.validate(
                EventType.DO_BRIX, json("{\"schemaVersion\":1,\"brix\":12.5}")));
    }

    @Test
    void rejectsBrixOutOfRange() throws Exception {
        assertThrows(EventPayloadValidator.ValidationException.class, () -> EventPayloadValidator.validate(
                EventType.DO_BRIX, json("{\"schemaVersion\":1,\"brix\":99}")));
    }

    @Test
    void rejectsBrixWithTooManyDecimals() throws Exception {
        assertThrows(EventPayloadValidator.ValidationException.class, () -> EventPayloadValidator.validate(
                EventType.DO_BRIX, json("{\"schemaVersion\":1,\"brix\":12.55}")));
    }

    @Test
    void rejectsNonPositiveQuantity() throws Exception {
        assertThrows(EventPayloadValidator.ValidationException.class, () -> EventPayloadValidator.validate(
                EventType.XUONG_GIONG, json("{\"schemaVersion\":1,\"quantity\":0}")));
    }

    @Test
    void rejectsUnknownSchemaVersion() throws Exception {
        assertThrows(EventPayloadValidator.ValidationException.class, () -> EventPayloadValidator.validate(
                EventType.GHI_CHU, json("{\"schemaVersion\":2}")));
    }

    @Test
    void rejectsNonObjectPayload() throws Exception {
        assertThrows(EventPayloadValidator.ValidationException.class, () -> EventPayloadValidator.validate(
                EventType.GHI_CHU, json("\"not-an-object\"")));
    }
}
