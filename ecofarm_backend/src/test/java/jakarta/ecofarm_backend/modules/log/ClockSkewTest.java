package jakarta.ecofarm_backend.modules.log;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClockSkewTest {

    @Test
    void nullDeviceTimeProducesNoSkewAndNoReview() {
        assertNull(ClockSkew.skewMs(null, 1000L));
        assertFalse(ClockSkew.needsReview(null, 100L));
    }

    @Test
    void skewIsServerMinusDevice() {
        assertEquals(500L, ClockSkew.skewMs(1000L, 1500L));
        assertEquals(-500L, ClockSkew.skewMs(2000L, 1500L));
    }

    @Test
    void reviewFlaggedOnlyWhenAbsoluteSkewExceedsThreshold() {
        assertTrue(ClockSkew.needsReview(400L, 300L));
        assertTrue(ClockSkew.needsReview(-400L, 300L));
        assertFalse(ClockSkew.needsReview(300L, 300L));
    }
}
