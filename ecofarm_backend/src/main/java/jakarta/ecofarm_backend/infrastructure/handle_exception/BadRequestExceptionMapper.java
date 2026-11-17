package jakarta.ecofarm_backend.infrastructure.handle_exception;

import jakarta.ecofarm_backend.common.dto.response.ExceptionResponse;
import jakarta.ecofarm_backend.common.exception.BadRequestException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {

    @Override
    public Response toResponse(BadRequestException e) {
        ExceptionResponse response = ExceptionResponse.builder()
                .code(e.getCode())
                .isSuccess(false)
                .message(e.getMessage())
                .errors(e.getErrors().isEmpty() ? null : e.getErrors())
                .build();

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(response)
                .build();
    }
}
