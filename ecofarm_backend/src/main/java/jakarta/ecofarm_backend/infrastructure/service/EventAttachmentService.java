package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.EventAttachment;

public interface EventAttachmentService {

    EventAttachment persist(EventAttachment attachment);
}
