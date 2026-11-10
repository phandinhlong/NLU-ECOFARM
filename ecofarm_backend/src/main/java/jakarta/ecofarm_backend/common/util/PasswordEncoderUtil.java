package jakarta.ecofarm_backend.common.util;

import jakarta.ecofarm_backend.common.constant.SecurityConstant;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordEncoderUtil {

    private static final int SALT_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();


    public static String encode(String rawPassword) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = sha256(salt, rawPassword);
        return Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }


    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) return false;
        String[] parts = encodedPassword.split("\\$");
        if (parts.length != 2) return false;
        try {
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expected = Base64.getDecoder().decode(parts[1]);
            byte[] actual = sha256(salt, rawPassword);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] sha256(byte[] salt, String rawPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance(SecurityConstant.PASSWORD_HASH_ALGORITHM);
            md.update(salt);
            return md.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 invalidate", e);
        }
    }
}