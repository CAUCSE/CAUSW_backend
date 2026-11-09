package net.causw.app.main.domain.admin.emailcampaign.service.dto;

import java.util.Map;

public record EmailCampaignTargetPreviewResult(
	long recipientCount,
	Map<String, Long> admissionYearDistribution,
	Map<String, Long> departmentDistribution,
	Map<String, Long> academicStatusDistribution) {
}
