package net.causw.app.main.domain.admin.emailcampaign.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.admin.emailcampaign.config.EmailCampaignProperties;
import net.causw.app.main.domain.admin.emailcampaign.entity.EmailCampaign;
import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignStatus;
import net.causw.app.main.shared.exception.errorcode.EmailCampaignErrorCode;

@Component
public class EmailCampaignValidator {

	/**
	 * 캠페인 제목과 대상자 존재 여부를 검증한다.
	 * @param subject 캠페인 제목
	 * @param targetCount 대상자 수
	 */
	public void validateCreate(String subject, int targetCount) {
		if (subject == null || subject.isBlank() || subject.length() > 255) {
			throw EmailCampaignErrorCode.EMAIL_CAMPAIGN_INVALID_SUBJECT.toBaseException();
		}
		if (targetCount == 0) {
			throw EmailCampaignErrorCode.EMAIL_CAMPAIGN_RECIPIENT_EMPTY.toBaseException();
		}
	}

	/**
	 * DRAFT 상태와 관리자가 입력한 제목·수신자 수 확인값을 검증한다.
	 * @param campaign 발송할 캠페인
	 * @param confirmedSubject 관리자가 재입력한 제목
	 * @param confirmedRecipientCount 관리자가 확인한 수신자 수
	 */
	public void validateSend(EmailCampaign campaign, String confirmedSubject, long confirmedRecipientCount) {
		if (campaign.getStatus() != EmailCampaignStatus.DRAFT) {
			throw EmailCampaignErrorCode.EMAIL_CAMPAIGN_SEND_ALREADY_REQUESTED.toBaseException();
		}
		if (!campaign.getSubject().equals(confirmedSubject)
			|| campaign.getRecipientCount() != confirmedRecipientCount) {
			throw EmailCampaignErrorCode.EMAIL_CAMPAIGN_CONFIRMATION_MISMATCH.toBaseException();
		}
	}

	/**
	 * 실제 발송 전에 feature flag와 필수 처리량 설정을 검증한다.
	 * @param properties 이메일 캠페인 설정
	 */
	public void validateConfiguration(EmailCampaignProperties properties) {
		if (!properties.isEnabled()) {
			throw EmailCampaignErrorCode.EMAIL_CAMPAIGN_SES_DISABLED.toBaseException();
		}
		if (properties.getFromAddress() == null || properties.getFromAddress().isBlank()
			|| properties.getMaxSendRate() <= 0 || properties.getMaxConcurrency() <= 0
			|| properties.getBatchSize() <= 0 || properties.getMaxAttempts() <= 0) {
			throw EmailCampaignErrorCode.EMAIL_CAMPAIGN_SES_CONFIGURATION_INVALID.toBaseException();
		}
	}
}
