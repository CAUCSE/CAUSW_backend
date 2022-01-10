package net.causw.infrastructure;

import net.causw.config.MailProperties;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.ServiceUnavailableException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Component
public class GoogleMailSender {
    private static final String GOOGLE_MAIL = "causwdev2021@gmail.com";

    private final MailProperties mailProperties;
    private final JavaMailSender mailSender;

    public GoogleMailSender(MailProperties mailProperties) {
        this.mailProperties = mailProperties;
        this.mailSender = getMailSender();
    }

    private JavaMailSender getMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(this.mailProperties.getHost());
        mailSender.setUsername(this.mailProperties.getUsername());
        mailSender.setPassword(this.mailProperties.getPassword());
        mailSender.setPort(this.mailProperties.getPort());

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", true);
        properties.put("mail.smtp.starttls.enable", true);
        properties.put("mail.smtp.starttls.required", true);
        properties.put("mail.smtp.ssl.trust", this.mailProperties.getHost());

        mailSender.setJavaMailProperties(properties);
        mailSender.setDefaultEncoding("UTF-8");
        return mailSender;
    }

    public void sendMail(
            String to,
            String title,
            String content
    ) {
        try {
            MimeMessage message = this.mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, false, "UTF-8");

            messageHelper.setSubject(title);
            messageHelper.setText(content, true);
            messageHelper.setFrom(GOOGLE_MAIL);

            messageHelper.setTo(to);
            this.mailSender.send(message);
        } catch (MailException | MessagingException exception) {
            throw new ServiceUnavailableException(ErrorCode.SERVICE_UNAVAILABLE, "이메일을 전송할 수 없습니다.");
        }
    }
}
