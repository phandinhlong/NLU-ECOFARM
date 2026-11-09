package jakarta.ecofarm_backend.infrastructure.handle_exception;

import jakarta.ecofarm_backend.common.exception.PermissionDeniedException;
import jakarta.ecofarm_backend.common.response.ExceptionResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class PermissionDeniedExceptionMapper  implements ExceptionMapper<PermissionDeniedException> {
    @Override
    public Response toResponse(PermissionDeniedException e) {
        ExceptionResponse exceptionResponse = ExceptionResponse.builder().code(e.getCode()).message(e.getMessage()).build();

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(exceptionResponse)
                .build();
    }
}
