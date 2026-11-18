package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.common.dto.request.AuditLogCriteria;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.AuditLog;

import java.util.List;

public interface AuditLogRepository {
    void persist(AuditLog log);

    AuditLog findById(Long id);

    List<AuditLog> findPage(AuditLogCriteria criteria);

    Long count(AuditLogCriteria criteria);
}
