package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.AuditLog;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.AuditLogRepository;
import jakarta.ecofarm_backend.infrastructure.service.AuditLogService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuditLogServiceImpl implements AuditLogService {

    @Inject
    private AuditLogRepository auditLogRepository;

    @Override
    public void persist(AuditLog log) {
        this.auditLogRepository.persist(log);
    }

    @Override
    public AuditLog findById(Long id) {
        return this.auditLogRepository.findById(id);
    }
}
