package net.causw.app.main.shared.infra.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.ServiceUnavailableException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GoogleMailSender {
	private final JavaMailSender javaMailSender;
	@Value("${spring.mail.username}")
	private String from;

	public void sendNewPasswordMail(
		String to,
		String password) {
		String title = "[중앙대학교 소프트웨어학부 동문네트워크] 계정 임시 비밀번호 안내";
		String content = "<div style=\"font-family: Arial, sans-serif; font-size: 16px; color: #333333; background-color: #f9f9f9; padding: 20px; border: 1px solid #dddddd; border-radius: 5px; max-width: 600px; margin: 0 auto;\">"
			+ "<div style=\"padding: 15px; background-color: #ffffff; border-radius: 5px; box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);\">"
			+ "<p style=\"font-size: 18px; font-weight: bold; color: #2c3e50;\">계정 임시 비밀번호 발급</p>"
			+ "<p style=\"line-height: 1.6;\">안녕하세요,</p>"
			+ "<p style=\"line-height: 1.6;\">귀하의 계정에 대한 임시 비밀번호가 <strong style=\"color: #e74c3c;\">[" + password
			+ "]</strong>로 설정되었습니다.</p>"
			+ "<p style=\"line-height: 1.6;\">임시 비밀번호를 사용하여 동문네트워크에 로그인하신 후, 보안을 위해 새 비밀번호로 변경해 주시기 바랍니다.</p>"
			+ "<p style=\"line-height: 1.6;\"><a href='https://causw.co.kr/' style=\"color: #3498db;\">CCSSAA 웹사이트로 이동하기</a></p>"
			+ "</div>"
			+ "<p style=\"font-weight: bold; text-align: center; margin-top: 20px; color: #2c3e50;\">중앙대학교 소프트웨어학부 ICT 위원회</p>"
			+ "<div style=\"font-size: 14px; color: #999999; margin-top: 20px; text-align: center;\">"
			+ "<p>서울특별시 동작구 흑석로 84, 중앙대학교 208관 117호</p>"
			+ "<p>이메일: caucsedongne@gmail.com</p>"
			+ "</div>"
			+ "</div>";

		this.sendMail(to, title, content);
	}

	public void sendEmailVerificationMail(String to, String verificationCode) {
		String title = "[중앙대학교 소프트웨어학부 동문네트워크] 이메일 인증 코드 안내";
		String content = "<div style=\"font-family: Arial, sans-serif; font-size: 16px; color: #333333; background-color: #f9f9f9; padding: 20px; border: 1px solid #dddddd; border-radius: 5px; max-width: 600px; margin: 0 auto;\">"
			+ "<div style=\"padding: 15px; background-color: #ffffff; border-radius: 5px; box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);\">"
			+ "<p style=\"font-size: 18px; font-weight: bold; color: #2c3e50;\">이메일 인증 코드 발급</p>"
			+ "<p style=\"line-height: 1.6;\">안녕하세요,</p>"
			+ "<p style=\"line-height: 1.6;\">아래의 인증 코드를 입력하여 이메일 인증을 완료해 주세요.</p>"
			+ "<p style=\"font-size: 24px; font-weight: bold; color: #e74c3c; text-align: center; letter-spacing: 4px;\">"
			+ verificationCode + "</p>"
			+ "<p style=\"line-height: 1.6;\">인증 코드는 <strong>10분간</strong> 유효합니다.</p>"
			+ "<p style=\"line-height: 1.6;\">본인이 요청하지 않은 경우 이 메일을 무시하시기 바랍니다.</p>"
			+ "</div>"
			+ "<p style=\"font-weight: bold; text-align: center; margin-top: 20px; color: #2c3e50;\">중앙대학교 소프트웨어학부 ICT 위원회</p>"
			+ "<div style=\"font-size: 14px; color: #999999; margin-top: 20px; text-align: center;\">"
			+ "<p>서울특별시 동작구 흑석로 84, 중앙대학교 208관 117호</p>"
			+ "<p>이메일: caucsedongne@gmail.com</p>"
			+ "</div>"
			+ "</div>";

		this.sendMail(to, title, content);
	}

	public void sendMail(
		String to,
		String title,
		String content) {
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
