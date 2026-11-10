package jakarta.ecofarm_backend.infrastructure.handle_exception;

import jakarta.ecofarm_backend.common.exception.JwtInvalidException;
import jakarta.ecofarm_backend.common.dto.response.ExceptionResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JwtInvalidExceptionMapper implements ExceptionMapper<JwtInvalidException> {

    @Override
    public Response toResponse(JwtInvalidException e) {
        ExceptionResponse exceptionResponse = ExceptionResponse.builder().code(e.getCode()).isSuccess(false).message(e.getMessage()).build();

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(exceptionResponse)
                .build();
    }
}
