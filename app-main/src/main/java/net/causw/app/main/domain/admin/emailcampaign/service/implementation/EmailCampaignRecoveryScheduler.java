package net.causw.app.main.domain.admin.emailcampaign.service.implementation;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.admin.emailcampaign.config.EmailCampaignProperties;
import net.causw.app.main.domain.admin.emailcampaign.entity.EmailCampaign;
import net.causw.app.main.domain.admin.emailcampaign.enums.EmailCampaignStatus;
import net.causw.app.main.domain.admin.emailcampaign.repository.EmailCampaignRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailCampaignRecoveryScheduler {

	private final EmailCampaignRepository campaignRepository;
	private final EmailCampaignDispatchStore dispatchStore;
	private final EmailCampaignDispatcher dispatcher;
	private final EmailCampaignProperties properties;

	@Scheduled(fixedDelayString = "${app.email-campaign.recovery-interval-ms:30000}")
	public void recover() {
		if (!properties.isEnabled()) {
			return;
		}
		LocalDateTime now = LocalDateTime.now();
		dispatchStore.releaseStaleClaims(now.minusSeconds(properties.getClaimTimeoutSeconds()), now);
		campaignRepository.findByStatusIn(List.of(EmailCampaignStatus.QUEUED, EmailCampaignStatus.SENDING))
			.stream()
			.map(EmailCampaign::getId)
			.forEach(dispatcher::dispatch);
	}
}
