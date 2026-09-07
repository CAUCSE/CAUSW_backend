package net.causw.app.main.domain.admin.emailcampaign.service.implementation;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.admin.audit.enums.AdminAuditLogCategory;
import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogCreateCommand;
import net.causw.app.main.domain.admin.audit.service.implementation.AdminAuditLogWriter;
import net.causw.app.main.domain.admin.emailcampaign.entity.EmailCampaign;
import net.causw.app.main.domain.admin.emailcampaign.repository.EmailCampaignRepository;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailCampaignAuditLogWriter {

	private static final String TARGET_TYPE = "EMAIL_CAMPAIGN";

	private final AdminAuditLogWriter adminAuditLogWriter;
	private final EmailCampaignRepository campaignRepository;
	private final UserRepository userRepository;

	public void writeCreate(EmailCampaign campaign, User actor) {
		write(campaign, actor, "EMAIL_CAMPAIGN_CREATE", "이메일 캠페인 생성", "이메일 캠페인을 생성했습니다.");
	}

	public void writeSendRequest(EmailCampaign campaign, User actor) {
		write(campaign, actor, "EMAIL_CAMPAIGN_SEND_REQUEST", "이메일 캠페인 발송 요청", "이메일 캠페인 발송을 요청했습니다.");
	}

	public void writeCompletion(String campaignId) {
		campaignRepository.findById(campaignId).ifPresent(campaign -> userRepository
			.findById(campaign.getSendRequestedByUserId())
			.ifPresent(actor -> write(
				campaign, actor, "EMAIL_CAMPAIGN_COMPLETE", "이메일 캠페인 발송 완료", "이메일 캠페인 발송이 종료되었습니다.")));
	}

	private void write(EmailCampaign campaign, User actor, String actionType, String description, String summary) {
		adminAuditLogWriter.write(new AdminAuditLogCreateCommand(
			AdminAuditLogCategory.EMAIL_CAMPAIGN,
			actionType,
			description,
			actor.getId(),
			actor.getEmail(),
			actor.getName(),
			actor.getStudentId(),
			TARGET_TYPE,
			campaign.getId(),
			null,
			null,
			null,
			summary,
			metadata(campaign)));
	}

	private Map<String, Object> metadata(EmailCampaign campaign) {
		Map<String, Object> metadata = new LinkedHashMap<>();
		metadata.put("campaignId", campaign.getId());
		metadata.put("recipientCount", campaign.getRecipientCount());
		metadata.put("status", campaign.getStatus());
		metadata.put("sentCount", campaign.getSentCount());
		metadata.put("failedCount", campaign.getFailedCount());
		metadata.put("skippedCount", campaign.getSkippedCount());
		return metadata;
	}
}
