package net.causw.app.main.domain.community.report.api.v2.dto.response;

import java.time.LocalDateTime;

import net.causw.app.main.domain.community.report.enums.ReportReason;

public record CommentReportResponse(
	String reportId,
	String commentId,
	ReportReason reportReason,
	LocalDateTime createdAt) {
}
