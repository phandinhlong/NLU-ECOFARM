package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.infrastructure.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class JwtServiceImpl implements JwtService {

    private static final Logger LOG = Logger.getLogger(JwtServiceImpl.class.getName());
    private static final String CLAIM_TYPE = "type";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    @Inject
    @ConfigProperty(name = "jwt.secret")
    String secret;

    @Inject
    @ConfigProperty(name = "jwt.issuer", defaultValue = "my-backend")
    String issuer;

    @Inject
    @ConfigProperty(name = "jwt.access.expiration.minutes", defaultValue = "15")
    long accessMinutes;

    @Inject
    @ConfigProperty(name = "jwt.refresh.expiration.days", defaultValue = "7")
    long refreshDays;

    private SecretKey key;

    @PostConstruct
    void init() {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("jwt.secret phải dài ít nhất 32 byte cho HS256");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    @Override
    public String generateAccessToken(String email) {
        return buildToken(email, TYPE_ACCESS, Duration.ofMinutes(accessMinutes));
    }

    @Override
    public String generateRefreshToken(String email) {
        return buildToken(email, TYPE_REFRESH, Duration.ofDays(refreshDays));
    }

    @Override
    public Boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            LOG.log(Level.FINE, "JWT không hợp lệ: {0}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getEmailFromJwtToken(String token) {
        return parseClaims(token).getSubject();
    }


    private String buildToken(String email, String type, Duration ttl) {
        Date now = new Date();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(email)
                .issuer(issuer)
                .claim(CLAIM_TYPE, type)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttl.toMillis()))
                .signWith(key)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}