package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.AuditLog;

public interface AuditLogService {

    void persist(AuditLog log);

    AuditLog findById(Long id);
}
