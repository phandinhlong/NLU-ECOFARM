package jakarta.ecofarm_backend.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventData {

    private Integer seq;
    private String eventType;
    private String status;
    private Long recordedAt;
    private String eventHash;
    private String actorRef;

    // JPQL constructor expression: tránh hydrate entity (cột có thể NULL)
    public EventData(Integer seq, String eventType, String status, Long recordedAt, String eventHash, Long recordedById) {
        this.seq = seq;
        this.eventType = eventType;
        this.status = status;
        this.recordedAt = recordedAt;
        this.eventHash = eventHash;
        this.actorRef = recordedById != null ? "usr_" + recordedById : null;
    }
}
