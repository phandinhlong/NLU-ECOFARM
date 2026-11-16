package jakarta.ecofarm_backend.modules.log;

import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps the LOG domain exceptions to the HTTP status the API contract and acceptance tests expect:
 * a write conflict (locked batch, bad transition, reused idempotency key) is 409, and a payload
 * validation failure is 400. Without these, JAX-RS would surface both as 500.
 */
public final class LogExceptionMappers {

    private LogExceptionMappers() {
    }

    @Provider
    public static class ConflictMapper implements ExceptionMapper<EventWriteGuard.ConflictException> {
        @Override
        public Response toResponse(EventWriteGuard.ConflictException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(BaseResponse.build(e.getMessage(), false))
                    .build();
        }
    }

    @Provider
    public static class ValidationMapper implements ExceptionMapper<EventPayloadValidator.ValidationException> {
        @Override
        public Response toResponse(EventPayloadValidator.ValidationException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(BaseResponse.build(e.getMessage(), false))
                    .build();
        }
    }
}
