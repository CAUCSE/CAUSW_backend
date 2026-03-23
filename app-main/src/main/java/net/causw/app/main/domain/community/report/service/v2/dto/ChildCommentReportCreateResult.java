package net.causw.app.main.domain.community.report.service.v2.dto;

import java.time.LocalDateTime;

import net.causw.app.main.domain.community.report.enums.ReportReason;

import lombok.Builder;

@Builder
public record ChildCommentReportCreateResult(
	String reportId,
	String childCommentId,
	ReportReason reportReason,
	LocalDateTime createdAt) {
}
