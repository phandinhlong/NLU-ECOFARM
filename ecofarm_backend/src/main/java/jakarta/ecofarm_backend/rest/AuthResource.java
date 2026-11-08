package jakarta.ecofarm_backend.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ejb.Stateless;

import java.util.HashMap;
import java.util.Map;

/**
 * Login placeholder - to be implemented later
 * For now, accepts any username/password and returns a dummy token
 */
@Path("/api/v1/auth")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @POST
    @Path("/login")
    public Map<String, Object> login(@QueryParam("username") String username, @QueryParam("password") String password) {
        // TODO: Implement real authentication (e.g., password hash check, JWT issuance)
        // Currently accepts any credentials and returns a dummy token
        Map<String, Object> response = new HashMap<>();
        response.put("access_token", "dummy-token-" + System.currentTimeMillis());
        response.put("token_type", "Bearer");
        response.put("expires_in", 3600);
        response.put("username", username);
        return response;
    }
}
