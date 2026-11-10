package jakarta.ecofarm_backend.config;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Properties;

@ApplicationScoped
public class MailConfig {

    @Inject
    @ConfigProperty(name = "mail.smtp.host", defaultValue = "smtp.gmail.com")
    private String host;

    @Inject
    @ConfigProperty(name = "mail.smtp.port", defaultValue = "587")
    private String port;

    @Inject
    @ConfigProperty(name = "mail.smtp.auth", defaultValue = "true")
    private String auth;

    @Inject
    @ConfigProperty(name = "mail.smtp.starttls.enable", defaultValue = "true")
    private String starttls;

    @Inject
    @ConfigProperty(name = "mail.username", defaultValue = "your@gmail.com")
    private String username;

    @Inject
    @ConfigProperty(name = "mail.password", defaultValue = "your_app_password")
    private String password;

    @Getter
    @Inject
    @ConfigProperty(name = "mail.from", defaultValue = "your@gmail.com")
    private String from;

    private Session session;

    @PostConstruct
    void init() {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.starttls.enable", starttls);

        this.session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    @Produces
    public Session getSession() {
        return this.session;
    }
}