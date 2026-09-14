package net.causw.app.main.shared.infra.mail.ses;

import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignFailureType;

import lombok.Getter;

@Getter
public class SesEmailSendException extends RuntimeException {

	private final EmailCampaignFailureType failureType;
	private final String safeErrorCode;

	/**
	 * dispatcher가 재시도 여부를 결정할 수 있는 분류 정보를 포함한 예외를 생성한다.
	 * @param failureType 실패 유형
	 * @param safeErrorCode 개인정보가 없는 오류 코드
	 * @param message 외부에 노출 가능한 일반 오류 설명
	 * @param cause 원본 SDK 예외
	 */
	public SesEmailSendException(
		EmailCampaignFailureType failureType, String safeErrorCode, String message, Throwable cause) {
		super(message, cause);
		this.failureType = failureType;
		this.safeErrorCode = safeErrorCode;
	}
}
