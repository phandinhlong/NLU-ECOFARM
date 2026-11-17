package jakarta.ecofarm_backend.infrastructure.persistence.repository.impl;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.AuditLog;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.AuditLogRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.time.Instant;
import java.util.List;

@Stateless
public class AuditLogRepositoryImpl implements AuditLogRepository {

    @PersistenceContext
    private EntityManager em;

    public void persist(AuditLog log) {
        em.persist(log);
    }

    public AuditLog findById(Long id) {
        return em.find(AuditLog.class, id);
    }
    
}
