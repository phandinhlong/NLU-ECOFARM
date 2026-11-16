package jakarta.ecofarm_backend.modules.log;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventWriteGuardTest {

    @Test
    void lockedBatchRejectsWritesCaseInsensitively() {
        assertThrows(EventWriteGuard.ConflictException.class,
                () -> EventWriteGuard.requireBatchWritable("anchoring"));
    }

    @Test
    void activeBatchAllowsWrites() {
        assertDoesNotThrow(() -> EventWriteGuard.requireBatchWritable("ACTIVE"));
    }

    @Test
    void onlyConfirmedEventCanBeApproved() {
        assertThrows(EventWriteGuard.ConflictException.class,
                () -> EventWriteGuard.requireApprovable(EventStatus.PENDING));
        assertDoesNotThrow(() -> EventWriteGuard.requireApprovable(EventStatus.CONFIRMED));
    }

    @Test
    void supersededOrPendingEventCannotBeAmended() {
        assertThrows(EventWriteGuard.ConflictException.class,
                () -> EventWriteGuard.requireAmendable(EventStatus.SUPERSEDED));
        assertThrows(EventWriteGuard.ConflictException.class,
                () -> EventWriteGuard.requireAmendable(EventStatus.PENDING));
        assertDoesNotThrow(() -> EventWriteGuard.requireAmendable(EventStatus.APPROVED));
    }

    @Test
    void retryWithDifferentContentOrOwnerConflicts() {
        assertThrows(EventWriteGuard.ConflictException.class,
                () -> EventWriteGuard.requireRetryMatches(true, false));
        assertThrows(EventWriteGuard.ConflictException.class,
                () -> EventWriteGuard.requireRetryMatches(false, true));
        assertDoesNotThrow(() -> EventWriteGuard.requireRetryMatches(true, true));
    }

    @Test
    void recorderAliasIsStableAndHidesRawId() {
        String a = RecorderAlias.of(42, 1000L);
        String b = RecorderAlias.of(42, 1000L);
        org.junit.jupiter.api.Assertions.assertEquals(a, b);
        org.junit.jupiter.api.Assertions.assertFalse(a.contains("42"));
    }
}
