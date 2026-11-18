package jakarta.ecofarm_backend.infrastructure.persistence.repository.impl;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.EventAttachment;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.EventAttachmentRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class EventAttachmentRepositoryImpl implements EventAttachmentRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public EventAttachment persist(EventAttachment attachment) {
        em.persist(attachment);
        return attachment;
    }
}
