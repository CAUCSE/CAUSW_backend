package net.causw.app.main.domain.community.report.service.v2.dto;

import net.causw.app.main.domain.community.report.enums.ReportReason;
import net.causw.app.main.domain.user.account.entity.user.User;

public record PostReportCreateCommand(
	String postId,
	ReportReason reportReason,
	User reporter) {
}
