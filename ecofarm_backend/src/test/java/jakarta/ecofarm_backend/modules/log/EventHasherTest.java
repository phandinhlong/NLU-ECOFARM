package jakarta.ecofarm_backend.modules.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class EventHasherTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private com.fasterxml.jackson.databind.JsonNode json(String s) throws Exception {
        return mapper.readTree(s);
    }

    @Test
    void sameContentDifferentKeyOrderHashesEqually() throws Exception {
        assertEquals(
                EventHasher.eventHash(json("{\"a\":1,\"b\":2}"), List.of()),
                EventHasher.eventHash(json("{\"b\":2,\"a\":1}"), List.of()));
    }

    @Test
    void attachmentOrderDoesNotChangeHash() throws Exception {
        assertEquals(
                EventHasher.eventHash(json("{\"a\":1}"), List.of("aa", "bb")),
                EventHasher.eventHash(json("{\"a\":1}"), List.of("bb", "aa")));
    }

    @Test
    void differentAttachmentContentChangesHash() throws Exception {
        assertNotEquals(
                EventHasher.eventHash(json("{\"a\":1}"), List.of("aa")),
                EventHasher.eventHash(json("{\"a\":1}"), List.of("cc")));
    }

    @Test
    void batchIdHashDependsOnSalt() {
        assertNotEquals(EventHasher.batchIdHash(7, null), EventHasher.batchIdHash(7, 99L));
    }

    @Test
    void sha256HexMatchesKnownVector() {
        // SHA-256 of the empty input.
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                EventHasher.sha256Hex(new byte[0]));
    }
}
