package jakarta.ecofarm_backend.common.dto;

public class RecallInfo {
    boolean recalled;
    String reason;

    public RecallInfo() {}

    public boolean isRecalled() { return recalled; }
    public void setRecalled(boolean recalled) { this.recalled = recalled; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
