package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.EventAttachment;
import jakarta.ecofarm_backend.infrastructure.persistence.repository.EventAttachmentRepository;
import jakarta.ecofarm_backend.infrastructure.service.EventAttachmentService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EventAttachmentServiceImpl implements EventAttachmentService {

    @Inject
    private EventAttachmentRepository eventAttachmentRepository;

    @Override
    public EventAttachment persist(EventAttachment attachment) {
        return eventAttachmentRepository.persist(attachment);
    }
}
