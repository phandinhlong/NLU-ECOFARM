package jakarta.ecofarm_backend.infrastructure.handle_exception;

import jakarta.ecofarm_backend.common.dto.response.ExceptionResponse;
import jakarta.ecofarm_backend.common.exception.InternalServerException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class InternalServerExceptionMapper implements ExceptionMapper<InternalServerException> {

    @Override
    public Response toResponse(InternalServerException e) {
        ExceptionResponse body = ExceptionResponse.builder()
                .code(e.getCode())
                .isSuccess(false)
                .message(e.getMessage())
                .build();

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
