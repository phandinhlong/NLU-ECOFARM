package jakarta.ecofarm_backend.dto;

public class ProofEvent {
    private Integer seq;
    private String eventType;
    private Object payload;

    public ProofEvent() {}

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
