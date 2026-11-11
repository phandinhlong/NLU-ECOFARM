package jakarta.ecofarm_backend.common.dto;

public class RecallRequest {
    private String reason;

    public RecallRequest() {}

    public RecallRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
