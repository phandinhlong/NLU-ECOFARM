package jakarta.ecofarm_backend.infrastructure.service.impl;

import jakarta.annotation.PreDestroy;
import jakarta.ecofarm_backend.config.MailConfig;
import jakarta.ecofarm_backend.infrastructure.service.EmailService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LogManager.getLogger(EmailServiceImpl.class);

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Inject
    private MailConfig mailConfig;

    @Override
    public void send(String to, String subject, String content) {
        this.executorService.submit(() -> {
            try {
                MimeMessage message = new MimeMessage(this.mailConfig.getSession());
                message.setFrom(new InternetAddress(this.mailConfig.getFrom()));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                message.setSubject(subject, "UTF-8");
                message.setContent(content, "text/html; charset=UTF-8");

                Transport.send(message);
                log.info("Email sent successfully to {}", to);
            } catch (MessagingException e) {
                log.error("Failed to send email to {}", to, e);
            }
        });
    }

    @Override
    public void sendActiveUserEmail(String to, String backendActivateUserUrl) {
        this.executorService.submit(() -> {
            try {
                String content = buildActivationEmail(backendActivateUserUrl);

                MimeMessage message = new MimeMessage(this.mailConfig.getSession());
                message.setFrom(new InternetAddress(this.mailConfig.getFrom()));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                message.setSubject("Kích hoạt tài khoản EcoFarm", "UTF-8");
                message.setContent(content, "text/html; charset=UTF-8");

                Transport.send(message);
                log.info("Email sent successfully to {}", to);
            } catch (MessagingException e) {
                log.error("Failed to send email to {}", to, e);
            }
        });
    }

    private String buildActivationEmail(String activateUrl) {
        String url = escapeHtml(activateUrl);
        return """
        <!DOCTYPE html>
        <html lang="vi">
        <body style="margin:0;padding:0;background-color:#f4f6f8;font-family:Arial,Helvetica,sans-serif;">
          <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f8;padding:24px 0;">
            <tr>
              <td align="center">
                <table role="presentation" width="480" cellpadding="0" cellspacing="0" style="background-color:#ffffff;border-radius:8px;padding:32px;">
                  <tr>
                    <td style="font-size:20px;font-weight:bold;color:#222222;padding-bottom:12px;">
                      Chào mừng bạn đến với EcoFarm
                    </td>
                  </tr>
                  <tr>
                    <td style="font-size:15px;line-height:22px;color:#444444;padding-bottom:24px;">
                      Cảm ơn bạn đã đăng ký. Vui lòng bấm nút bên dưới để kích hoạt tài khoản.
                      Link có hiệu lực trong <b>15 phút</b>.
                    </td>
                  </tr>
                  <tr>
                    <td align="center" style="padding-bottom:24px;">
                      <a href="%s" target="_blank"
                         style="display:inline-block;background-color:#2e7d32;color:#ffffff;text-decoration:none;
                                font-size:16px;font-weight:bold;padding:14px 32px;border-radius:6px;">
                        Kích hoạt tài khoản
                      </a>
                    </td>
                  </tr>
                  <tr>
                    <td style="font-size:12px;line-height:18px;color:#888888;">
                      Nếu nút không bấm được, hãy copy link sau dán vào trình duyệt:<br>
                      <a href="%s" style="color:#2e7d32;word-break:break-all;">%s</a>
                      <br><br>
                      Nếu bạn không đăng ký tài khoản, hãy bỏ qua email này.
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
          </table>
        </body>
        </html>
        """.formatted(url, url, url);
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    @PreDestroy
    public void destroy() {
        this.executorService.shutdown();
    }
}

