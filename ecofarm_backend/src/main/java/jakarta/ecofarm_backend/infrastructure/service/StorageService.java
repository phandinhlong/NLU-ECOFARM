package jakarta.ecofarm_backend.infrastructure.service;

import jakarta.servlet.http.Part;

public interface StorageService {

    String upload(Part file);

    void delete(String fileKey);

    String getUrl(String fileKey);

}
