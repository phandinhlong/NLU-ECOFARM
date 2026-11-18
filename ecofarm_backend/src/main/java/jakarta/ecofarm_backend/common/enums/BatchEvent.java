package jakarta.ecofarm_backend.common.enums;

public enum BatchEvent {
    START_GROWING,
    HARVEST,
    PASS_QC,
    REJECT,
    START_ANCHORING,
    ANCHOR_CONFIRMED,
    ANCHOR_FAILED,
    RETRY_ANCHORING,
    SHIP,
    REJECT_DELIVERY,
    RECALL
}