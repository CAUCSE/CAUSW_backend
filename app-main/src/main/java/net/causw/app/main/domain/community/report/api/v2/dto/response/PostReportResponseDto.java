package net.causw.app.main.domain.community.report.api.v2.dto.response;

import java.time.LocalDateTime;

import net.causw.app.main.domain.community.report.enums.ReportReason;

public record PostReportResponseDto(
	String reportId,
	String postId,
	ReportReason reportReason,
	LocalDateTime createdAt) {
}
