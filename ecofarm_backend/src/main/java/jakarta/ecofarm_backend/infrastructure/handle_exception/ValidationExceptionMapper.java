package jakarta.ecofarm_backend.infrastructure.handle_exception;

import jakarta.ecofarm_backend.common.response.BaseResponse;
import jakarta.ecofarm_backend.common.response.ConstraintViolationExceptionResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.Map;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ConstraintViolationException e) {
        Map<String, String> details = new LinkedHashMap<>();

        for (ConstraintViolation<?> v : e.getConstraintViolations()) {

            String path = v.getPropertyPath().toString();
            String field = path.substring(path.lastIndexOf('.') + 1);
            details.put(field, v.getMessage());
        }

        ConstraintViolationExceptionResponse exceptionResponse =
                ConstraintViolationExceptionResponse.builder().details(details).isSuccess(false).build();

        return Response.status(400).type(MediaType.APPLICATION_JSON).entity(exceptionResponse).build();
    }
}