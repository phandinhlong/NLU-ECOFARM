package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.common.enums.EventType;
import jakarta.ecofarm_backend.infrastructure.service.EventSchemaService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class EventSchemaServiceImpl implements EventSchemaService {

    private static final Map<EventType, List<String>> REQUIRED = Map.of(
            EventType.SOWING, List.of("varietyId", "sowDate", "seedLot", "quantity"),
            EventType.POLLINATION, List.of("pollinationDate", "method", "flowerCount"),
            EventType.FERTILIZE_IRRIGATE, List.of("fertilizerName", "amountKg", "unit", "appliedAt"),
            EventType.POLLINATION_ISOLATION, List.of("stoppedAt", "isolationDays", "reason"),
            EventType.BRIX_MEASUREMENT, List.of("brix", "sampleCount", "measuredAt"),
            EventType.HARVEST, List.of("harvestDate", "totalWeightKg", "grade"),
            EventType.NOTE, List.of("text"),
            EventType.PRUNE_SHOOTS, List.of("shootsRemoved", "note"),
            EventType.IRRIGATE, List.of("waterLiters", "appliedAt"),
            EventType.TASK_ASSIGNED, List.of("taskId", "taskType", "workerId", "assignedAt")
    );

    @Override
    public List<String> validate(EventType type, Map<String, Object> data) {
        List<String> fields = REQUIRED.get(type);
        if (fields == null) {
            return Collections.emptyList();
        }
        if (data == null) {
            data = Collections.emptyMap();
        }
        List<String> errors = new ArrayList<>();
        for (String field : fields) {
            Object value = data.get(field);
            if (value == null || (value instanceof String s && s.isBlank())) {
                errors.add("Thiếu trường bắt buộc: " + field);
            }
        }
        if (type == EventType.NOTE) {
            Object text = data.get("text");
            int length = text instanceof String s ? s.length() : 0;
            if (length < 1 || length > 2000) {
                errors.add("Nội dung ghi chú phải từ 1 đến 2000 ký tự");
            }
        }
        return errors;
    }
}
