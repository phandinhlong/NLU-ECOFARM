package jakarta.ecofarm_backend.infrastructure.service;

public interface EmailService {
    void send(String to, String subject, String content);

    void sendActiveUserEmail(String to, String backendActivateUserUrl );
}
