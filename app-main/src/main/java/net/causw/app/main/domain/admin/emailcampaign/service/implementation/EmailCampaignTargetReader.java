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

	/**
	 * 지정한 필터에 해당하는 현재 발송 대상을 조회한다.
	 * @param filter 발송 대상 필터
	 * @return 발송 대상 스냅샷 목록
	 */
	public List<EmailCampaignTarget> findTargets(EmailCampaignFilter filter) {
		return targetQueryRepository.findTargets(filter);
	}
}
