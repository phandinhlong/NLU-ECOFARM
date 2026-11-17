package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.AuditLog;

import java.time.Instant;
import java.util.List;

public interface AuditLogRepository {
    void persist(AuditLog log);

    AuditLog findById(Long id);
}
