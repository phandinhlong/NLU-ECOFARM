package jakarta.ecofarm_backend.infrastructure.handle_exception;


import jakarta.ecofarm_backend.common.exception.EntityNotFoundException;
import jakarta.ecofarm_backend.common.response.ExceptionResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class EntityNotFoundExceptionMapper implements ExceptionMapper<EntityNotFoundException> {


    @Override
    public Response toResponse(EntityNotFoundException e) {
        ExceptionResponse exceptionResponse = ExceptionResponse.builder().code(e.getCode()).isSuccess(false).message(e.getMessage()).build();

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(exceptionResponse)
                .build();
    }
}