package jakarta.ecofarm_backend.modules.log;

import java.nio.charset.StandardCharsets;

/**
 * Deterministic, non-reversible alias for the recorder, used inside the shared/hashed payload so the
 * ledger can attribute an event to a stable actor without exposing the real user id or email.
 *
 * <p>The alias is derived from the user id and the batch salt, so the same person on the same batch
 * is consistent across events while the raw id never enters the hashed content. The real
 * {@code recorded_by} relation is still stored on the entity for server-side authorization and is
 * excluded from the DTO and the payload.
 */
public final class RecorderAlias {

    private RecorderAlias() {
    }

    public static String of(long userId, Long batchSalt) {
        String material = "recorder:" + userId + ":" + (batchSalt == null ? "" : batchSalt);
        return EventHasher.sha256Hex(material.getBytes(StandardCharsets.UTF_8)).substring(0, 16);
    }
}
