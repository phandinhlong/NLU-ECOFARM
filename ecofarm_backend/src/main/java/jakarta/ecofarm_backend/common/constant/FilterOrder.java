package jakarta.ecofarm_backend.common.constant;

import jakarta.ws.rs.Priorities;

public class FilterOrder {
    public static final int LOGGING  = Priorities.AUTHENTICATION - 300;
    public static final int CORS     = Priorities.AUTHENTICATION - 200;
    public static final int AUTHN    = Priorities.AUTHENTICATION;
}
