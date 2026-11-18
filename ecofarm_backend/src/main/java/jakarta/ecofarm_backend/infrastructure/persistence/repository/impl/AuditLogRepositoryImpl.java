package jakarta.ecofarm_backend.infrastructure.persistence.repository.impl;

import jakarta.ecofarm_backend.common.dto.request.AuditLogCriteria;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.AuditLog;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.AuditLogRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;


@Stateless
public class AuditLogRepositoryImpl implements AuditLogRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void persist(AuditLog log) {
        em.persist(log);
    }

    @Override
    public AuditLog findById(Long id) {
        return em.find(AuditLog.class, id);
    }

    @Override
    public List<AuditLog> findPage(AuditLogCriteria criteria) {
        StringBuilder jpql = new StringBuilder("SELECT a FROM AuditLog a LEFT JOIN FETCH a.actor WHERE 1 = 1");
        Map<String, Object> parameters = appendFilters(jpql, criteria);
        if (criteria.getLastIndex() != null) {
            jpql.append(" AND a.id < :lastIndex");
            parameters.put("lastIndex", criteria.getLastIndex());
        }
        jpql.append(" ORDER BY a.id DESC");

        TypedQuery<AuditLog> query = em.createQuery(jpql.toString(), AuditLog.class);
        parameters.forEach(query::setParameter);
        query.setMaxResults(AuditLogCriteria.LENGTH + 1);
        return query.getResultList();
    }

    @Override
    public Long count(AuditLogCriteria criteria) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(a) FROM AuditLog a WHERE 1 = 1");
        Map<String, Object> parameters = appendFilters(jpql, criteria);
        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        parameters.forEach(query::setParameter);
        return query.getSingleResult();
    }

    private Map<String, Object> appendFilters(StringBuilder jpql, AuditLogCriteria criteria) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        if (criteria.getActorId() != null) {
            jpql.append(" AND a.actor.id = :actorId");
            parameters.put("actorId", criteria.getActorId());
        }
        if (criteria.getAction() != null) {
            jpql.append(" AND a.action = :action");
            parameters.put("action", criteria.getAction());
        }
        if (criteria.getEntity() != null) {
            jpql.append(" AND a.entity = :entity");
            parameters.put("entity", criteria.getEntity());
        }
        if (criteria.getEntityId() != null) {
            jpql.append(" AND a.entityId = :entityId");
            parameters.put("entityId", criteria.getEntityId());
        }
        if (criteria.getFrom() != null) {
            jpql.append(" AND a.createdAt >= :from");
            parameters.put("from", criteria.getFrom());
        }
        if (criteria.getTo() != null) {
            jpql.append(" AND a.createdAt <= :to");
            parameters.put("to", criteria.getTo());
        }
        return parameters;
    }
}
