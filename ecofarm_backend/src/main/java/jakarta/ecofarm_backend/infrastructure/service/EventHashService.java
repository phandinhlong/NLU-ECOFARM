package jakarta.ecofarm_backend.infrastructure.service;

import java.util.Map;

public interface EventHashService {
    String hash(Map<String, Object> payload);
    String canonicalJson(Map<String, Object> payload);
}
