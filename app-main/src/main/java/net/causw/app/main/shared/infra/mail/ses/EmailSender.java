package net.causw.app.main.shared.infra.mail.ses;

public interface EmailSender {

	/**
	 * 단일 수신자에게 HTML 이메일을 발송한다.
	 * @param message 발신·수신·제목·정제 본문을 포함한 메시지
	 * @return 외부 이메일 서비스 접수 결과
	 */
	EmailSendResult send(EmailMessage message);
}
