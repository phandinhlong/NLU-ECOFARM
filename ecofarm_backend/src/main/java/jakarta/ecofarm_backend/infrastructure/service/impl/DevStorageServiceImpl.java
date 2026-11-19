package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.ecofarm_backend.infrastructure.service.StorageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


@ApplicationScoped
public class DevStorageServiceImpl implements StorageService {

    private static final Path STORAGE_PATH =
            Paths.get("/dev_uploads")
                    .toAbsolutePath()
                    .normalize();

    public DevStorageServiceImpl() {
        try {
            Files.createDirectories(STORAGE_PATH);
        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize storage", e);
        }
    }

    @Override
    public String upload(Part file) {
        if (file == null || file.getSize() == 0) {
            throw new IllegalArgumentException("File is empty");
        }

        try {
            String originalName = Paths.get(file.getSubmittedFileName())
                    .getFileName().toString();

            String extension = "";

            int dotIndex = originalName.lastIndexOf(".");
            if (dotIndex >= 0) {
                extension = originalName.substring(dotIndex);
            }

            String fileKey = UUID.randomUUID() + extension;

            Path target = STORAGE_PATH.resolve(fileKey).normalize();

            if (!target.startsWith(STORAGE_PATH)) {
                throw new SecurityException("Invalid file path");
            }

            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target);
            }

            return fileKey;

        } catch (IOException e) {
            throw new RuntimeException("Cannot upload file", e);
        }
    }

    @Override
    public void delete(String fileKey) {
        try {
            Path target = STORAGE_PATH.resolve(fileKey).normalize();

            if (!target.startsWith(STORAGE_PATH)) {
                throw new SecurityException("Invalid file path");
            }

            Files.deleteIfExists(target);

        } catch (IOException e) {
            throw new RuntimeException("Cannot delete file", e);
        }
    }

    @Override
    public String getUrl(String fileKey) {
        return "/uploads/" + fileKey;
    }
}
