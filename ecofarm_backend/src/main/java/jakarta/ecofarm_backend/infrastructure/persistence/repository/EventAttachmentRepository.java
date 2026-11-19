package jakarta.ecofarm_backend.infrastructure.persistence.repository;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.EventAttachment;

public interface EventAttachmentRepository {

    EventAttachment persist(EventAttachment attachment);
}
