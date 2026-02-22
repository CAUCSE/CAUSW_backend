package net.causw.app.main.domain.community.report.api.v2.dto.response;

import net.causw.app.main.domain.community.report.enums.ReportReason;

public record PostReportReasonResponse(
	ReportReason reason,
	String description) {

	public static PostReportReasonResponse from(ReportReason reason) {
		return new PostReportReasonResponse(reason, reason.getDescription());
	}
}
