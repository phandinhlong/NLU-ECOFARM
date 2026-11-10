package jakarta.ecofarm_backend.infrastructure.handle_exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.ecofarm_backend.common.dto.response.ExceptionResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JsonParseExceptionMapper implements ExceptionMapper<JsonProcessingException> {
    @Override
    public Response toResponse(com.fasterxml.jackson.core.JsonProcessingException e) {

        ExceptionResponse exceptionResponse = ExceptionResponse.builder().code(4000).isSuccess(false).message(e.getMessage()).build();

        return Response.status(4000)
                .type(MediaType.APPLICATION_JSON)
                .entity(exceptionResponse)
                .build();
    }
}