package jakarta.ecofarm_backend.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum AllowedMimeType {

    JPEG("image/jpeg") {
        @Override
        public boolean matchesMagic(byte[] data) {
            return data.length >= 3
                    && (data[0] & 0xFF) == 0xFF
                    && (data[1] & 0xFF) == 0xD8
                    && (data[2] & 0xFF) == 0xFF;
        }
    },

    PNG("image/png") {
        @Override
        public boolean matchesMagic(byte[] data) {
            return data.length >= 8
                    && (data[0] & 0xFF) == 0x89
                    && (data[1] & 0xFF) == 0x50
                    && (data[2] & 0xFF) == 0x4E
                    && (data[3] & 0xFF) == 0x47
                    && (data[4] & 0xFF) == 0x0D
                    && (data[5] & 0xFF) == 0x0A
                    && (data[6] & 0xFF) == 0x1A
                    && (data[7] & 0xFF) == 0x0A;
        }
    },

    WEBP("image/webp") {
        @Override
        public boolean matchesMagic(byte[] data) {
            return data.length >= 12
                    && (data[0] & 0xFF) == 'R'
                    && (data[1] & 0xFF) == 'I'
                    && (data[2] & 0xFF) == 'F'
                    && (data[3] & 0xFF) == 'F'
                    && (data[8]  & 0xFF) == 'W'
                    && (data[9]  & 0xFF) == 'E'
                    && (data[10] & 0xFF) == 'B'
                    && (data[11] & 0xFF) == 'P';
        }
    },

    PDF("application/pdf") {
        @Override
        public boolean matchesMagic(byte[] data) {
            // %PDF-
            return data.length >= 4
                    && (data[0] & 0xFF) == '%'
                    && (data[1] & 0xFF) == 'P'
                    && (data[2] & 0xFF) == 'D'
                    && (data[3] & 0xFF) == 'F';
        }
    };

    private final String mimeType;

    public abstract boolean matchesMagic(byte[] data);

    public static AllowedMimeType fromMimeType(String mimeType) {
        if (mimeType == null) return null;
        String cleanMime = mimeType.split(";")[0].trim();
        for (AllowedMimeType type : values()) {
            if (type.mimeType.equalsIgnoreCase(cleanMime)) {
                return type;
            }
        }
        return null;
    }
}
