package jakarta.ecofarm_backend.infrastructure.persistence.repository.impl;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.EventAttachment;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.EventAttachmentRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class EventAttachmentRepositoryImpl implements EventAttachmentRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void persist(EventAttachment attachment) {
        this.entityManager.persist(attachment);
    }

    @Override
    public EventAttachment merge(EventAttachment attachment) {
        return this.entityManager.merge(attachment);
    }

    @Override
    public List<EventAttachment> findActiveByFileKey(String fileKey) {
        return this.entityManager.createQuery(
                        "select a from EventAttachment a where a.fileKey = :key and a.isActive = true",
                        EventAttachment.class)
                .setParameter("key", fileKey)
                .getResultList();
    }

    @Override
    public List<EventAttachment> findUnlinkedByFileKey(String fileKey) {
        return this.entityManager.createQuery(
                        "select a from EventAttachment a where a.fileKey = :key and a.event is null",
                        EventAttachment.class)
                .setParameter("key", fileKey)
                .getResultList();
    }

    @Override
    public List<String> findSha256ByFileKeys(List<String> fileKeys) {
        if (fileKeys == null || fileKeys.isEmpty()) return List.of();
        return this.entityManager.createQuery(
                        "select a.sha256 from EventAttachment a where a.fileKey in :keys", String.class)
                .setParameter("keys", fileKeys)
                .getResultList();
    }

    @Override
    public List<EventAttachment> findActiveByEventIdOrderById(Long eventId) {
        return this.entityManager.createQuery(
                        "select a from EventAttachment a where a.event.id = :id and a.isActive = true order by a.id",
                        EventAttachment.class)
                .setParameter("id", eventId)
                .getResultList();
    }

    @Override
    public List<EventAttachment> findActiveByEventIdsOrderById(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) return List.of();
        return this.entityManager.createQuery(
                        "select a from EventAttachment a where a.event.id in :eventIds and a.isActive = true order by a.id",
                        EventAttachment.class)
                .setParameter("eventIds", eventIds)
                .getResultList();
    }
}
