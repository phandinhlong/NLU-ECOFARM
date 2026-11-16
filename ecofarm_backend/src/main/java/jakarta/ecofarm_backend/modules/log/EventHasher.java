package jakarta.ecofarm_backend.modules.log;

import com.fasterxml.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Computes the content hashes anchored to the ledger for a cultivation event.
 *
 * <p>The event hash binds the canonical payload and the sorted attachment hashes, so a tampered
 * image or a reordered payload changes it. Server-side file paths and mutable download URLs are
 * deliberately excluded: only the SHA-256 of each file's bytes participates, matching the approved
 * rule that hashed content must not contain changeable URLs.
 *
 * <p>The exact concatenation and salt handling are shared with LDG/PCK/VER and are not yet frozen;
 * {@code eventHash}/{@code batchIdHash} here are the project's working formula and must be reconciled
 * with that contract before anchoring to a real chain.
 */
public final class EventHasher {

    private EventHasher() {
    }

    /** SHA-256, lower-case hex. */
    public static String sha256Hex(byte[] bytes) {
        MessageDigest digest = newDigest();
        return toHex(digest.digest(bytes));
    }

    /**
     * Event hash over the canonical payload and the attachment content hashes.
     *
     * <p>Attachment hashes are sorted so their submission order does not affect the result, then
     * appended after the canonical payload separated by a newline that cannot occur unescaped inside
     * the canonical JSON.
     */
    public static String eventHash(JsonNode payload, List<String> attachmentSha256) {
        StringBuilder material = new StringBuilder(CanonicalJson.canonicalize(payload));
        attachmentSha256.stream().sorted().forEach(h -> material.append('\n').append(h));
        return sha256Hex(material.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Batch id hash used to route and group outbox records without exposing the raw batch id.
     * Salt is the batch's {@code salt_enc} rendered as decimal; an absent salt hashes the id alone.
     */
    public static String batchIdHash(long batchId, Long salt) {
        String material = salt == null ? Long.toString(batchId) : batchId + ":" + salt;
        return sha256Hex(material.getBytes(StandardCharsets.UTF_8));
    }

    private static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(Character.forDigit((b >> 4) & 0xF, 16));
            hex.append(Character.forDigit(b & 0xF, 16));
        }
        return hex.toString();
    }
}
