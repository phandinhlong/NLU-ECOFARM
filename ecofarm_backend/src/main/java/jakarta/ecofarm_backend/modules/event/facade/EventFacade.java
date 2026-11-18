package jakarta.ecofarm_backend.modules.event.facade;

import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;
import jakarta.ecofarm_backend.common.dto.request.EventRecordCommand;

public interface EventFacade {
    CultivationEvent record(EventRecordCommand command);
}
