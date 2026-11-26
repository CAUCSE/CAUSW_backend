package net.causw.app.main.domain.admin.emailcampaign.api.v2.dto.response;

import java.util.Map;

public record EmailCampaignTargetPreviewResponse(
	long recipientCount,
	Map<String, Long> admissionYearDistribution,
	Map<String, Long> departmentDistribution,
	Map<String, Long> academicStatusDistribution) {
}
