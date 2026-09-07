package net.causw.app.main.domain.admin.emailcampaign.service.implementation;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.admin.emailcampaign.entity.EmailCampaign;
import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignRecipientStatus;
import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignStatus;
import net.causw.app.main.domain.admin.emailcampaign.repository.EmailCampaignRepository;
import net.causw.app.main.domain.admin.emailcampaign.repository.query.EmailCampaignQueryRepository;
import net.causw.app.main.domain.admin.emailcampaign.repository.query.EmailCampaignRecipientItem;
import net.causw.app.main.domain.admin.emailcampaign.repository.query.EmailCampaignSummary;
import net.causw.app.main.shared.exception.errorcode.EmailCampaignErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailCampaignReader {

	private final EmailCampaignRepository campaignRepository;
	private final EmailCampaignQueryRepository campaignQueryRepository;

	public EmailCampaign getById(String campaignId) {
		return campaignRepository.findById(campaignId)
			.orElseThrow(EmailCampaignErrorCode.EMAIL_CAMPAIGN_NOT_FOUND::toBaseException);
	}

	public Page<EmailCampaignSummary> findCampaigns(
		EmailCampaignStatus status, LocalDateTime from, LocalDateTime to, Pageable pageable) {
		return campaignQueryRepository.findCampaigns(status, from, to, pageable);
	}

	public Page<EmailCampaignRecipientItem> findRecipients(
		String campaignId, EmailCampaignRecipientStatus status, Pageable pageable) {
		getById(campaignId);
		return campaignQueryRepository.findRecipients(campaignId, status, pageable);
	}
}
