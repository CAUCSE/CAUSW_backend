package net.causw.app.main.domain.admin.emailcampaign.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.admin.emailcampaign.repository.query.EmailCampaignTarget;
import net.causw.app.main.domain.admin.emailcampaign.repository.query.EmailCampaignTargetQueryRepository;
import net.causw.app.main.domain.admin.emailcampaign.service.dto.EmailCampaignFilter;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailCampaignTargetReader {

	private final EmailCampaignTargetQueryRepository targetQueryRepository;

	public List<EmailCampaignTarget> findTargets(EmailCampaignFilter filter) {
		return targetQueryRepository.findTargets(filter);
	}
}
