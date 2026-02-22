package net.causw.app.main.domain.community.report.api.v2.dto.response;

import net.causw.app.main.domain.community.report.enums.ReportReason;

public record PostReportReasonResponseDto(
	ReportReason reason,
	String description) {
//
	public static PostReportReasonResponseDto from(ReportReason reason) {
		return new PostReportReasonResponseDto(reason, reason.getDescription());
	}
}
