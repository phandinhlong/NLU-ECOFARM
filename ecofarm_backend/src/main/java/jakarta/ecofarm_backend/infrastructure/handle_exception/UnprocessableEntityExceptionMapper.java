package jakarta.ecofarm_backend.infrastructure.handle_exception;

import jakarta.ecofarm_backend.common.dto.response.ExceptionResponse;
import jakarta.ecofarm_backend.common.exception.UnprocessableEntityException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class UnprocessableEntityExceptionMapper implements ExceptionMapper<UnprocessableEntityException> {

    @Override
    public Response toResponse(UnprocessableEntityException e) {
        ExceptionResponse response = ExceptionResponse.builder()
                .code(e.getCode())
                .isSuccess(false)
                .message(e.getMessage())
                .errors(e.getErrors().isEmpty() ? null : e.getErrors())
                .build();

        return Response.status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(response)
                .build();
    }
}
