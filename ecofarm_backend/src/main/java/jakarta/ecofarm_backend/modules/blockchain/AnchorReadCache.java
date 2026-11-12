package jakarta.ecofarm_backend.modules.blockchain;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class AnchorReadCache {

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.verify.cache.ttl-seconds", defaultValue = "300")
    private long ttlSeconds;

    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    public String getPayload(String txHash) {
        Entry entry = entries.get(txHash);
        if (entry == null || entry.isExpired(ttlSeconds)) return null;
        return entry.payload;
    }

    public String getPayloadAllowStale(String txHash) {
        Entry entry = entries.get(txHash);
        return entry == null ? null : entry.payload;
    }

    public void put(String txHash, String payload) {
        entries.put(txHash, new Entry(payload, System.currentTimeMillis()));
    }

    private static final class Entry {
        final String payload;
        final long storedAt;

        Entry(String payload, long storedAt) {
            this.payload = payload;
            this.storedAt = storedAt;
        }

        boolean isExpired(long ttlSeconds) {
            return System.currentTimeMillis() - storedAt > ttlSeconds * 1000L;
        }
    }
}
