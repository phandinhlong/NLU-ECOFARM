package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.infrastructure.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.crypto.SecretKey;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class JwtServiceImpl implements JwtService {

    private static final Logger LOG = Logger.getLogger(JwtServiceImpl.class.getName());
    private static final String CLAIM_TYPE = "type";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";
    public static final String TYPE_ACCOUNT_ACTIVATION_TOKEN ="account-activation";

    private String secret = "change-me-to-a-long-random-string-at-least-32-chars";
    private String issuer = "my-backend";
    private long accessMinutes = 15;
    private long refreshDays = 7;

    private SecretKey key;

    @PostConstruct
    void init() {
        try {
            Config config = ConfigProvider.getConfig();
            config.getOptionalValue("jwt.secret", String.class).ifPresent(v -> this.secret = v);
            config.getOptionalValue("jwt.issuer", String.class).ifPresent(v -> this.issuer = v);
            config.getOptionalValue("jwt.access.expiration.minutes", Long.class).ifPresent(v -> this.accessMinutes = v);
            config.getOptionalValue("jwt.refresh.expiration.days", Long.class).ifPresent(v -> this.refreshDays = v);
        } catch (Throwable ignored) {
            try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/microprofile-config.properties")) {
                if (is != null) {
                    Properties props = new Properties();
                    props.load(is);
                    if (props.getProperty("jwt.secret") != null) this.secret = props.getProperty("jwt.secret");
                    if (props.getProperty("jwt.issuer") != null) this.issuer = props.getProperty("jwt.issuer");
                    if (props.getProperty("jwt.access.expiration.minutes") != null) {
                        this.accessMinutes = Long.parseLong(props.getProperty("jwt.access.expiration.minutes"));
                    }
                    if (props.getProperty("jwt.refresh.expiration.days") != null) {
                        this.refreshDays = Long.parseLong(props.getProperty("jwt.refresh.expiration.days"));
                    }
                }
            } catch (Throwable e) {
                // ignore
            }
        }

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

    @Override
    public String generateAccountActivationToken(String email) {
        return buildToken(email,TYPE_ACCOUNT_ACTIVATION_TOKEN , Duration.ofMinutes(accessMinutes));
    }

    @Override
    public Boolean hasTokenType(String token, String expectedType) {
        return parseClaims(token).get(CLAIM_TYPE, String.class).equals(expectedType);
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