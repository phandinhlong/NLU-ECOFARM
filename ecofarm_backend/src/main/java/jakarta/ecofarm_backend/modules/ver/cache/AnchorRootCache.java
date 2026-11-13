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

    public String get(String key) {
        if (key == null) {
            return null;
        }
        Entry entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        if (System.currentTimeMillis() >= entry.expiresAt) {
            cache.remove(key);
            return null;
        }
        return entry.root;
    }

    public void put(String key, String root) {
        if (key == null || root == null) {
            return;
        }
        cache.put(key, new Entry(root, System.currentTimeMillis() + ttlSeconds * 1000L));
    }
}
