package jakarta.ecofarm_backend.modules.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CanonicalJsonTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private String canon(String json) throws Exception {
        return CanonicalJson.canonicalize(mapper.readTree(json));
    }

    @Test
    void sortsObjectKeysRegardlessOfInputOrder() throws Exception {
        assertEquals("{\"a\":1,\"b\":2}", canon("{\"b\":2,\"a\":1}"));
    }

    @Test
    void normalizesTrailingZeroAndIntegralNumbers() throws Exception {
        assertEquals("{\"x\":1.5}", canon("{\"x\":1.500}"));
        assertEquals("{\"x\":2}", canon("{\"x\":2.0}"));
    }

    @Test
    void escapesControlCharactersAndKeepsVietnameseText() throws Exception {
        assertEquals("{\"k\":\"a\\nb\\\"c\"}", canon("{\"k\":\"a\\nb\\\"c\"}"));
        assertEquals("{\"note\":\"tưới nước\"}", canon("{\"note\":\"tưới nước\"}"));
    }

    @Test
    void canonicalizesNestedObjectsRecursively() throws Exception {
        assertEquals("{\"outer\":{\"a\":1,\"z\":2}}", canon("{\"outer\":{\"z\":2,\"a\":1}}"));
    }
}
