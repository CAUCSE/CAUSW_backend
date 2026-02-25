package net.causw.app.main.domain.community.report.service.v2.dto;

import java.time.LocalDateTime;

import net.causw.app.main.domain.community.report.entity.Report;
import net.causw.app.main.domain.community.report.enums.ReportReason;

import lombok.Builder;

@Builder
public record PostReportCreateResult(
	String reportId,
	String postId,
	ReportReason reportReason,
	LocalDateTime createdAt) {

	public static PostReportCreateResult from(Report report) {
		return new PostReportCreateResult(
			report.getId(),
			report.getTargetId(),
			report.getReportReason(),
			report.getCreatedAt());
	}
}
