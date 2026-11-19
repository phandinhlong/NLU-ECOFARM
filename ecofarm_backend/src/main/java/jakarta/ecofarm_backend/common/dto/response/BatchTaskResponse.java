package jakarta.ecofarm_backend.common.dto.response;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BatchTaskResponse {

    private Long id;
    @Positive
    private Long batchId;
    @Positive
    private Long workerId;
    private String workerName;
    private String taskType;
    private String note;
    @Positive
    private Long assignedById;
    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;
    private String status;
    @Positive
    private Long eventId;
    @Positive
    private Long completionEventId;
}
