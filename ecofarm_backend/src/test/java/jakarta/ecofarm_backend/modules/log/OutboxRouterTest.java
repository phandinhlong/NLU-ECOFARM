package jakarta.ecofarm_backend.modules.log;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class OutboxRouterTest {

    @Test
    void everyLogEventTypeRoutesPrivateOnly() {
        for (EventType type : EventType.values()) {
            assertEquals(OutboxRouter.Destination.PRIVATE_ONLY, OutboxRouter.route(type));
            assertFalse(OutboxRouter.reachesPublicChain(type), type + " must not reach the public chain");
        }
    }
}
