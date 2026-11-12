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

    private static final int LINK_EXPIRY_MINUTES = 15;

    private static final String ACTIVATION_SUBJECT = "Kích hoạt tài khoản EcoFarm";
    private static final String RESET_PASSWORD_SUBJECT = "Đặt lại mật khẩu EcoFarm";

    private static final String ACTION_EMAIL_TEMPLATE = """
            <!DOCTYPE html>
            <html lang="vi">
            <body style="margin:0;padding:0;background-color:#f4f6f8;font-family:Arial,Helvetica,sans-serif;">
              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f8;padding:24px 0;">
                <tr>
                  <td align="center">
                    <table role="presentation" width="480" cellpadding="0" cellspacing="0" style="background-color:#ffffff;border-radius:8px;padding:32px;">
                      <tr>
                        <td style="font-size:20px;font-weight:bold;color:#222222;padding-bottom:12px;">%s</td>
                      </tr>
                      <tr>
                        <td style="font-size:15px;line-height:22px;color:#444444;padding-bottom:24px;">
                          %s Link có hiệu lực trong <b>%d phút</b>.
                        </td>
                      </tr>
                      <tr>
                        <td align="center" style="padding-bottom:24px;">
                          <a href="%s" target="_blank"
                             style="display:inline-block;background-color:#2e7d32;color:#ffffff;text-decoration:none;
                                    font-size:16px;font-weight:bold;padding:14px 32px;border-radius:6px;">%s</a>
                        </td>
                      </tr>
                      <tr>
                        <td style="font-size:12px;line-height:18px;color:#888888;">
                          Nếu nút không bấm được, hãy copy link sau dán vào trình duyệt:<br>
                          <a href="%s" style="color:#2e7d32;word-break:break-all;">%s</a>
                          <br><br>%s
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Inject
    private MailConfig mailConfig;

    @Override
    public void send(String to, String subject, String content) {
        sendHtml(to, subject, content);
    }

    @Override
    public void sendActiveUserEmail(String to, String backendActivateUserUrl) {
        String content = buildActionEmail(
                "Chào mừng bạn đến với EcoFarm",
                "Cảm ơn bạn đã đăng ký. Vui lòng bấm nút bên dưới để kích hoạt tài khoản.",
                "Kích hoạt tài khoản",
                backendActivateUserUrl,
                "Nếu bạn không đăng ký tài khoản, hãy bỏ qua email này.");
        sendHtml(to, ACTIVATION_SUBJECT, content);
    }

    @Override
    public void sendForgotPasswordEmail(String to, String forgotPasswordUrl) {
        String content = buildActionEmail(
                "Đặt lại mật khẩu EcoFarm",
                "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. "
                        + "Vui lòng bấm nút bên dưới để tạo mật khẩu mới.",
                "Đặt lại mật khẩu",
                forgotPasswordUrl,
                "Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này. "
                        + "Mật khẩu của bạn sẽ không bị thay đổi.");
        sendHtml(to, RESET_PASSWORD_SUBJECT, content);
    }

    private void sendHtml(String to, String subject, String htmlContent) {
        executorService.submit(() -> {
            try {
                MimeMessage message = new MimeMessage(mailConfig.getSession());
                message.setFrom(new InternetAddress(mailConfig.getFrom()));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                message.setSubject(subject, "UTF-8");
                message.setContent(htmlContent, "text/html; charset=UTF-8");

                Transport.send(message);
                log.info("Email sent successfully to {}", to);
            } catch (MessagingException e) {
                log.error("Failed to send email to {}", to, e);
            }
        });
    }

    private String buildActionEmail(String title, String intro, String buttonText,
                                    String actionUrl, String footerNote) {
        String url = escapeHtml(actionUrl);
        return ACTION_EMAIL_TEMPLATE.formatted(
                escapeHtml(title),
                escapeHtml(intro),
                LINK_EXPIRY_MINUTES,
                url,
                escapeHtml(buttonText),
                url,
                url,
                escapeHtml(footerNote));
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    @PreDestroy
    public void destroy() {
        executorService.shutdown();
    }
}