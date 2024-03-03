package net.causw.infrastructure;

import lombok.RequiredArgsConstructor;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
@RequiredArgsConstructor
public class GoogleMailSender {
    private final JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String from;

    public void sendNewPasswordMail(
            String to,
            String password
    ) {
        String title = "중앙대학교 소프트웨어학부 동문네트워크 커뮤니티에서 임시 비밀번호를 알려 드립니다.";
        String content = "<div style=\"font-size: 16px;color: #4d4d4d;padding: 35px 0px 5px 0px;text-align: left;\">    " +
                "<div style=\"padding:10px 10px 10px 20px;\">        " +
                "<p>귀하의 계정 비밀번호가 [" + password + "]으로 초기화 되었습니다.</p>        " +
                "<p>임시 비밀번호로 동문네트워크에 로그인 후 새 비밀번호로 변경해 사용하시기 바랍니다.</p>    " +
                "</div>" +
                "<br><span style=\"font-weight: bold;\">중앙대학교 소프트웨어학부 동문네트워크</span> 드림<br>" +
                "</div>";
        this.sendMail(to, title, content);
    }

    public void sendMail(
            String to,
            String title,
            String content
    ) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, false,"UTF-8");
            messageHelper.setFrom(from);
            messageHelper.setSubject(title);
            messageHelper.setText(content, true);
            messageHelper.setTo(to);
            System.out.println("hi");
            javaMailSender.send(message);
        } catch (MailException | MessagingException exception) {
            throw new ServiceUnavailableException(ErrorCode.SERVICE_UNAVAILABLE, "이메일을 전송할 수 없습니다.");
        }
    }
}
