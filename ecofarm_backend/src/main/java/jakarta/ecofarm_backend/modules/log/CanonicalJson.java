package jakarta.ecofarm_backend.modules.log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Deterministic JSON serialization used as the hashing input for cultivation events.
 *
 * <p>Follows the parts of RFC 8785 (JSON Canonicalization Scheme) the project relies on: object
 * members sorted by key using UTF-16 code-unit order, no insignificant whitespace, and numbers
 * emitted without trailing zeros or a leading plus. The output is intentionally independent of the
 * order keys arrived in, so two devices that submit the same event produce the same bytes and the
 * same {@code eventHash}.
 *
 * <p>The hash formula shared with LDG/PCK/VER is not finalized; before anchoring to a real chain the
 * canonical form and salt handling must be confirmed against that contract. Until then this is the
 * single canonicalizer every LOG hash goes through so there is one place to reconcile.
 */
public final class CanonicalJson {

    private CanonicalJson() {
    }

    public static String canonicalize(JsonNode node) {
        StringBuilder out = new StringBuilder();
        write(node, out);
        return out.toString();
    }

    private static void write(JsonNode node, StringBuilder out) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            out.append("null");
            return;
        }
        if (node.isObject()) {
            writeObject((ObjectNode) node, out);
            return;
        }
        if (node.isArray()) {
            writeArray((ArrayNode) node, out);
            return;
        }
        if (node.isTextual()) {
            writeString(node.textValue(), out);
            return;
        }
        if (node.isBoolean()) {
            out.append(node.booleanValue() ? "true" : "false");
            return;
        }
        if (node.isNumber()) {
            out.append(canonicalNumber(node.decimalValue()));
            return;
        }
        throw new IllegalArgumentException("Unsupported JSON node for canonicalization: " + node.getNodeType());
    }

    private static void writeObject(ObjectNode node, StringBuilder out) {
        Map<String, JsonNode> sorted = new TreeMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            sorted.put(field.getKey(), field.getValue());
        }
        out.append('{');
        boolean first = true;
        for (Map.Entry<String, JsonNode> entry : sorted.entrySet()) {
            if (!first) {
                out.append(',');
            }
            first = false;
            writeString(entry.getKey(), out);
            out.append(':');
            write(entry.getValue(), out);
        }
        out.append('}');
    }

    private static void writeArray(ArrayNode node, StringBuilder out) {
        out.append('[');
        List<JsonNode> items = new ArrayList<>();
        node.forEach(items::add);
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                out.append(',');
            }
            write(items.get(i), out);
        }
        out.append(']');
    }

    /**
     * Minimal canonical number form: integral values print without a decimal point, other values use
     * {@link BigDecimal#stripTrailingZeros()} so {@code 1.50} and {@code 1.5} hash identically.
     */
    static String canonicalNumber(BigDecimal value) {
        BigDecimal stripped = value.stripTrailingZeros();
        if (stripped.scale() <= 0) {
            return stripped.toBigInteger().toString();
        }
        return stripped.toPlainString();
    }

    private static void writeString(String value, StringBuilder out) {
        out.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
    }
}
