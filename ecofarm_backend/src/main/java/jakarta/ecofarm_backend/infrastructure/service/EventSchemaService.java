package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.common.enums.EventType;

import java.util.List;
import java.util.Map;

public interface EventSchemaService {
    List<String> validate(EventType type, Map<String, Object> data);
}
