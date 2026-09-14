package net.causw.app.main.domain.admin.emailcampaign.service.implementation;

import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignFailureType;

public record EmailCampaignSendOutcome(
	Long recipientId,
	String messageId,
	EmailCampaignFailureType failureType,
	String errorCode,
	String errorDetail) {

	/**
	 * SES가 접수한 수신자 발송 결과를 생성한다.
	 * @param recipientId 수신자 ID
	 * @param messageId SES 메시지 ID
	 * @return 성공 결과
	 */
	public static EmailCampaignSendOutcome success(Long recipientId, String messageId) {
		return new EmailCampaignSendOutcome(recipientId, messageId, null, null, null);
	}

	/**
	 * 분류된 수신자 발송 실패 결과를 생성한다.
	 * @param recipientId 수신자 ID
	 * @param failureType 재시도 가능 여부를 나타내는 실패 유형
	 * @param errorCode 안전한 실패 코드
	 * @param errorDetail 개인정보가 없는 실패 설명
	 * @return 실패 결과
	 */
	public static EmailCampaignSendOutcome failure(
		Long recipientId, EmailCampaignFailureType failureType, String errorCode, String errorDetail) {
		return new EmailCampaignSendOutcome(recipientId, null, failureType, errorCode, errorDetail);
	}

	/**
	 * SES 메시지 ID 존재 여부로 성공 결과인지 판단한다.
	 * @return 성공 결과이면 true
	 */
	public boolean isSuccess() {
		return messageId != null;
	}
}
