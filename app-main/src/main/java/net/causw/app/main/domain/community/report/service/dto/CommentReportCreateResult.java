package net.causw.app.main.domain.community.report.service.dto;

import java.time.LocalDateTime;

import net.causw.app.main.domain.community.report.enums.ReportReason;

import lombok.Builder;

@Builder
public record CommentReportCreateResult(
	String reportId,
	String commentId,
	ReportReason reportReason,
	LocalDateTime createdAt) {
}
