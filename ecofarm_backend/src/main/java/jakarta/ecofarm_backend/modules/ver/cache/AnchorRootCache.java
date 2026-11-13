package jakarta.ecofarm_backend.modules.ver.cache;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class AnchorRootCache {

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.ver.cache.ttl-seconds", defaultValue = "300")
    private Integer ttlSeconds;

    private final ConcurrentHashMap<String, Entry> cache = new ConcurrentHashMap<>();

    private static final class Entry {
        private final String root;
        private final long expiresAt;

        Entry(String root, long expiresAt) {
            this.root = root;
            this.expiresAt = expiresAt;
        }
    }

    public String get(String contractAddress) {
        if (contractAddress == null) {
            return null;
        }
        Entry entry = cache.get(contractAddress);
        if (entry == null) {
            return null;
        }
        if (System.currentTimeMillis() >= entry.expiresAt) {
            cache.remove(contractAddress);
            return null;
        }
        return entry.root;
    }

    public void put(String contractAddress, String root) {
        if (contractAddress == null || root == null) {
            return;
        }
        cache.put(contractAddress, new Entry(root, System.currentTimeMillis() + ttlSeconds * 1000L));
    }
}
