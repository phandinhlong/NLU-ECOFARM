package jakarta.ecofarm_backend.modules.event.facade;

import jakarta.ecofarm_backend.common.dto.request.AmendEventRequest;
import jakarta.ecofarm_backend.common.dto.request.CreateEventRequest;
import jakarta.ecofarm_backend.common.dto.request.EventRecordCommand;
import jakarta.ecofarm_backend.common.dto.request.ReassignApproverRequest;
import jakarta.ecofarm_backend.common.dto.request.RejectEventRequest;
import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ecofarm_backend.common.dto.response.CursorPageResponse;
import jakarta.ecofarm_backend.common.dto.response.EventResponse;
import jakarta.ecofarm_backend.common.enums.EventReviewStatus;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.CultivationEvent;

public interface EventFacade {
    CultivationEvent record(EventRecordCommand command);

    EventResponse createEvent(Long batchId, CreateEventRequest request);

    BaseResponse<CursorPageResponse<EventResponse>> findEvents(Long batchId, Long lastIndex, EventReviewStatus reviewStatus);

    BaseResponse<CursorPageResponse<EventResponse>> findReviewQueue(Long lastIndex, Long greenhouseId, EventReviewStatus reviewStatus);

    BaseResponse<Long> countReviewQueue(Long greenhouseId, EventReviewStatus reviewStatus);

    BaseResponse<EventResponse> findEvent(Long id);

    BaseResponse<EventResponse> approve(Long id);

    BaseResponse<EventResponse> reject(Long id, RejectEventRequest request);

    BaseResponse<EventResponse> reassignApprover(Long id, ReassignApproverRequest request);

    BaseResponse<EventResponse> amend(Long id, AmendEventRequest request);
}
