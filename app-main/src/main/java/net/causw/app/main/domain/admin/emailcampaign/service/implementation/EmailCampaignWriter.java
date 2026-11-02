package net.causw.app.main.domain.admin.emailcampaign.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.admin.emailcampaign.entity.EmailCampaign;
import net.causw.app.main.domain.admin.emailcampaign.entity.EmailCampaignRecipient;
import net.causw.app.main.domain.admin.emailcampaign.repository.EmailCampaignRecipientRepository;
import net.causw.app.main.domain.admin.emailcampaign.repository.EmailCampaignRepository;
import net.causw.app.main.domain.admin.emailcampaign.repository.query.EmailCampaignTarget;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class EmailCampaignWriter {

	private final EmailCampaignRepository campaignRepository;
	private final EmailCampaignRecipientRepository recipientRepository;

	/**
	 * 캠페인을 먼저 저장하고 대상 사용자별 이메일 스냅샷을 수신자로 일괄 저장한다.
	 * @param campaign 저장할 캠페인
	 * @param targets 생성 시점 발송 대상
	 * @return 저장된 캠페인
	 */
	public EmailCampaign saveWithRecipients(EmailCampaign campaign, List<EmailCampaignTarget> targets) {
		EmailCampaign saved = campaignRepository.saveAndFlush(campaign);
		List<EmailCampaignRecipient> recipients = targets.stream()
			.map(target -> EmailCampaignRecipient.of(saved, target.userId(), target.email()))
			.toList();
		recipientRepository.saveAll(recipients);
		return saved;
	}
}
