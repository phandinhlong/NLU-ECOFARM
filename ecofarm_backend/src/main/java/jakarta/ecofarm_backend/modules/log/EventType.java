package jakarta.ecofarm_backend.modules.log;

import java.util.Arrays;
import java.util.Optional;

/**
 * Cultivation event types a client may record, plus the system-generated {@code EVENT_APPROVED}.
 *
 * <p>The wire code (what the client sends and what is stored in {@code event_type}) is kept stable
 * and separate from the enum name so renaming the enum never changes stored data or the API
 * contract. Clients must never submit {@link #EVENT_APPROVED} or {@link #THU_HOACH}: approval is
 * system-generated, and harvest creation is opened in commit 2 once batch-state transitions exist.
 */
public enum EventType {
    XUONG_GIONG("XUONG_GIONG", true),
    THU_PHAN("THU_PHAN", true),
    BON_PHAN("BON_PHAN", true),
    TUOI_NUOC("TUOI_NUOC", true),
    PHUN_VI_LUONG("PHUN_VI_LUONG", true),
    NGUNG_PHAN("NGUNG_PHAN", true),
    DO_BRIX("DO_BRIX", true),
    THU_HOACH("THU_HOACH", false),
    GHI_CHU("GHI_CHU", true),
    EVENT_APPROVED("EVENT_APPROVED", false);

    private final String code;
    private final boolean clientRecordable;

    EventType(String code, boolean clientRecordable) {
        this.code = code;
        this.clientRecordable = clientRecordable;
    }

    public String code() {
        return code;
    }

    /** Whether a client may create this type now. Harvest and approval are excluded in commit 1. */
    public boolean isClientRecordable() {
        return clientRecordable;
    }

    public static Optional<EventType> fromCode(String code) {
        return Arrays.stream(values()).filter(t -> t.code.equals(code)).findFirst();
    }
}
