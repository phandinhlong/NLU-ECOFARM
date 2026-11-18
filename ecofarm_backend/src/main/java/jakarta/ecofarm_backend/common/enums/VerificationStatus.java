package jakarta.ecofarm_backend.common.enums;

public enum VerificationStatus {
    UNVERIFIED,
    PENDING,
    VERIFIED;

    public static VerificationStatus fromAnchorStatus(AnchorStatus anchorStatus) {
        if (anchorStatus == null) {
            return UNVERIFIED;
        }
        switch (anchorStatus) {
            case CONFIRMED:
                return VERIFIED;
            case PENDING:
            case SUBMITTED:
                return PENDING;
            case FAILED:
            default:
                return UNVERIFIED;
        }
    }
}
