package jakarta.ecofarm_backend.infrastructure.handle_exception;

import jakarta.ecofarm_backend.common.dto.response.ExceptionResponse;
import jakarta.ecofarm_backend.common.exception.InvalidFileException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class InvalidFileExceptionMapper implements ExceptionMapper<InvalidFileException> {

    @Override
    public Response toResponse(InvalidFileException e) {
        ExceptionResponse body = ExceptionResponse.builder()
                .code(e.getCode())
                .isSuccess(false)
                .message(e.getMessage())
                .build();

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
