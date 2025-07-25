package net.causw.app.main.infrastructure.mail;

import lombok.RequiredArgsConstructor;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

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
        String title = "[중앙대학교 소프트웨어학부 동문네트워크] 계정 임시 비밀번호 안내";
        String content = "<div style=\"font-family: Arial, sans-serif; font-size: 16px; color: #333333; background-color: #f9f9f9; padding: 20px; border: 1px solid #dddddd; border-radius: 5px; max-width: 600px; margin: 0 auto;\">"
                + "<div style=\"padding: 15px; background-color: #ffffff; border-radius: 5px; box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);\">"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #2c3e50;\">계정 임시 비밀번호 발급</p>"
                + "<p style=\"line-height: 1.6;\">안녕하세요,</p>"
                + "<p style=\"line-height: 1.6;\">귀하의 계정에 대한 임시 비밀번호가 <strong style=\"color: #e74c3c;\">[" + password + "]</strong>로 설정되었습니다.</p>"
                + "<p style=\"line-height: 1.6;\">임시 비밀번호를 사용하여 동문네트워크에 로그인하신 후, 보안을 위해 새 비밀번호로 변경해 주시기 바랍니다.</p>"
                + "<p style=\"line-height: 1.6;\"><a href='https://causw.co.kr/' style=\"color: #3498db;\">동문네트워크 웹사이트로 이동하기</a></p>"
                + "</div>"
                + "<p style=\"font-weight: bold; text-align: center; margin-top: 20px; color: #2c3e50;\">중앙대학교 소프트웨어학부 동문네트워크</p>"
                + "<div style=\"font-size: 14px; color: #999999; margin-top: 20px; text-align: center;\">"
                + "<p>서울특별시 동작구 흑석로 84, 중앙대학교 소프트웨어학부 동문 네트워크</p>"
                + "<p>문의 전화: 02-123-4567 | 이메일: caucsedongne@gmail.com</p>"
                + "</div>"
                + "</div>";


        this.sendMail(to, title, content);
    }

    public void sendMail(
            String to,
            String title,
            String content
    ) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, false, "UTF-8");
            messageHelper.setFrom(from);
            messageHelper.setSubject(title);
            messageHelper.setText(content, true);
            messageHelper.setTo(to);
            messageHelper.setReplyTo(from);

            // 이메일 전송
            javaMailSender.send(message);
        } catch (MailException | MessagingException exception) {
            throw new ServiceUnavailableException(ErrorCode.SERVICE_UNAVAILABLE, "이메일을 전송할 수 없습니다.");
        }
    }
}
