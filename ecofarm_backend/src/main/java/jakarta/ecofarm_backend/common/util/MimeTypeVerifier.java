package jakarta.ecofarm_backend.common.util;

import jakarta.ecofarm_backend.common.enums.AllowedMimeType;
import jakarta.ecofarm_backend.common.enums.ErrorCode;
import jakarta.ecofarm_backend.common.exception.InvalidFileException;
import jakarta.servlet.http.Part;


public final class MimeTypeVerifier {


    public static final long MAX_FILE_SIZE = 20L * 1024 * 1024;

    private MimeTypeVerifier() { }

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


    public static void verify(String declaredMime, byte[] data) {
        AllowedMimeType allowedType = AllowedMimeType.fromMimeType(declaredMime);

        if (allowedType == null) {
            throw new InvalidFileException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }

        if (!allowedType.matchesMagic(data)) {
            throw new InvalidFileException(ErrorCode.FILE_CONTENT_MISMATCH);
        }
    }
}
