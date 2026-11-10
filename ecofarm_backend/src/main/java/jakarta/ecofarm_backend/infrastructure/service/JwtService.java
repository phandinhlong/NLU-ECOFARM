package jakarta.ecofarm_backend.infrastructure.service;

public interface JwtService {
    String generateAccessToken(String email);

    String generateRefreshToken(String email);

    Boolean validateToken(String token);

    String getEmailFromJwtToken(String token);

    String generateAccountActivationToken(String email);

    Boolean hasTokenType(String token, String type);
}
