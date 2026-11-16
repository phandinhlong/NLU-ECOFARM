package jakarta.ecofarm_backend.modules.log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

public final class LocalImageStore {
    public static final int MAX_BYTES = 5 * 1024 * 1024;
    private final Path root;
    private final int maxBytes;

    public LocalImageStore(Path root) {
        this(root, MAX_BYTES);
    }

    public LocalImageStore(Path root, int maxBytes) {
        if (maxBytes <= 0 || maxBytes == Integer.MAX_VALUE) throw new IllegalArgumentException("Invalid file size limit");
        this.root = root.toAbsolutePath().normalize();
        this.maxBytes = maxBytes;
    }

    public StoredImage save(byte[] bytes, String mimeType) throws IOException {
        if (bytes == null) throw new IllegalArgumentException("Invalid file");
        return save(new ByteArrayInputStream(bytes), mimeType);
    }

    public StoredImage save(InputStream input, String mimeType) throws IOException {
        if (input == null) throw new IllegalArgumentException("Invalid file");
        byte[] bytes = input.readNBytes(maxBytes + 1);
        if (bytes.length == 0 || bytes.length > maxBytes || !matches(bytes, mimeType)) {
            throw new IllegalArgumentException("Invalid file or file exceeds size limit");
        }
        try {
            String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
            Files.createDirectories(root);
            String key = UUID.randomUUID().toString();
            Files.write(root.resolve(key), bytes, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            return new StoredImage(key, digest, mimeType);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    public byte[] read(String key) throws IOException {
        try {
            if (!UUID.fromString(key).toString().equals(key)) throw new IllegalArgumentException("Invalid image key");
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid image key", e);
        }
        try (InputStream input = Files.newInputStream(root.resolve(key), StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS)) {
            return input.readAllBytes();
        }
    }

    private static boolean matches(byte[] data, String mimeType) {
        if ("image/jpeg".equals(mimeType)) return data.length >= 3 && (data[0] & 255) == 255 && (data[1] & 255) == 216 && (data[2] & 255) == 255;
        if ("image/png".equals(mimeType)) return data.length >= 8 && (data[0] & 255) == 137 && data[1] == 80 && data[2] == 78 && data[3] == 71 && data[4] == 13 && data[5] == 10 && data[6] == 26 && data[7] == 10;
        if ("image/webp".equals(mimeType)) return data.length >= 12 && data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F' && data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P';
        if ("application/pdf".equals(mimeType)) return data.length >= 5 && data[0] == '%' && data[1] == 'P' && data[2] == 'D' && data[3] == 'F' && data[4] == '-';
        return false;
    }

    public record StoredImage(String fileKey, String sha256, String mimeType) {}
}
