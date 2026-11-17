package jakarta.ecofarm_backend.modules.log.service;

import jakarta.ecofarm_backend.modules.log.LocalImageStore;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Bọc {@link LocalImageStore} cho thao tác tệp (ghi/đọc/xóa). CỐ Ý không gắn @Transactional: toàn bộ
 * I/O tệp phải chạy NGOÀI transaction DB để không giữ khóa dòng trong lúc ghi đĩa. Tầng facade chịu
 * trách nhiệm dọn tệp mồ côi (gọi deleteQuietly) nếu transaction ghi DB sau đó thất bại.
 */
@ApplicationScoped
public class AttachmentStorageService {

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.log.attachment.root", defaultValue = "/var/lib/ecofarm/log-attachments")
    private String attachmentRoot;

    @Inject
    @ConfigProperty(name = "nlu.eco.farm.log.attachment.max-bytes", defaultValue = "5242880")
    private int attachmentMaxBytes;

    private LocalImageStore store() {
        return new LocalImageStore(Path.of(attachmentRoot), attachmentMaxBytes);
    }

    /** Ghi một tệp từ luồng dữ liệu, trả về khóa + sha256 + mime để facade lưu vào DB sau đó. */
    public LocalImageStore.StoredImage save(InputStream input, String mimeType) throws IOException {
        return store().save(input, mimeType);
    }

    /** Đọc nội dung tệp theo khóa. */
    public byte[] read(String fileKey) throws IOException {
        return store().read(fileKey);
    }

    /** Xóa một tệp theo khóa; nuốt lỗi I/O vì đây là thao tác dọn dẹp best-effort. */
    public void deleteQuietly(String fileKey) {
        try {
            store().delete(fileKey);
        } catch (IOException ignored) {
            // Dọn dẹp best-effort: không che lỗi nghiệp vụ gốc bằng một lỗi I/O khi xóa.
        }
    }
}
