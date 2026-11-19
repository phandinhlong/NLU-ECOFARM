package jakarta.ecofarm_backend.common.util;

import jakarta.ecofarm_backend.common.enums.AllowedMimeType;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.exception.InvalidFileException;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.MediaType;

/**
 * Utility to verify uploaded files: size, MIME type whitelist, and magic bytes.
 */
public final class MimeTypeVerifier {

    /** Maximum file size: 20 MB. */
    public static final long MAX_FILE_SIZE = 20L * 1024 * 1024;

    private MimeTypeVerifier() { }

    /**
     * Validates upload EntityPart: not null, and MIME type is whitelisted.
     *
     * @param file EntityPart from multipart request
     * @throws InvalidFileException if file is invalid
     */
    public static void validateFile(EntityPart file) {
        if (file == null) {
            throw new InvalidFileException(ErrorCode.FILE_EMPTY);
        }
        MediaType mediaType = file.getMediaType();
        String contentType = mediaType != null ? mediaType.getType() + "/" + mediaType.getSubtype() : null;
        if (contentType == null || AllowedMimeType.fromMimeType(contentType) == null) {
            throw new InvalidFileException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
    }

    /**
     * Validates upload Part: not empty, size <= 20MB, and MIME type is whitelisted.
     *
     * @param file Part from multipart request
     * @throws InvalidFileException if file is invalid
     */
    public static void validateFile(Part file) {
        if (file == null || file.getSize() == 0) {
            throw new InvalidFileException(ErrorCode.FILE_EMPTY);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException(ErrorCode.FILE_TOO_LARGE);
        }
        String contentType = file.getContentType();
        if (contentType == null || AllowedMimeType.fromMimeType(contentType) == null) {
            throw new InvalidFileException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
    }

    /**
     * Verifies that declared MIME type is whitelisted and matches actual magic bytes.
     * Also validates byte length boundaries.
     *
     * @param declaredMime declared MIME type from client
     * @param data         file bytes in memory
     * @throws InvalidFileException if MIME is unsupported or magic bytes do not match
     */
    public static void verify(String declaredMime, byte[] data) {
        if (data == null || data.length == 0) {
            throw new InvalidFileException(ErrorCode.FILE_EMPTY);
        }
        if (data.length > MAX_FILE_SIZE) {
            throw new InvalidFileException(ErrorCode.FILE_TOO_LARGE);
        }

        AllowedMimeType allowedType = AllowedMimeType.fromMimeType(declaredMime);

        if (allowedType == null) {
            throw new InvalidFileException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }

        if (!allowedType.matchesMagic(data)) {
            throw new InvalidFileException(ErrorCode.FILE_CONTENT_MISMATCH);
        }
    }
}
