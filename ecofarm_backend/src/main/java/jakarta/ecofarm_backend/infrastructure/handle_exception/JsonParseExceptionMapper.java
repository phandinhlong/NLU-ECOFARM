package jakarta.ecofarm_backend.infrastructure.handle_exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class JsonParseExceptionMapper implements ExceptionMapper<JsonProcessingException> {
    @Override
    public Response toResponse(com.fasterxml.jackson.core.JsonProcessingException e) {
        return Response.status(400)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("code", 400, "message", "Body JSON invalid"))
                .build();
    }
}