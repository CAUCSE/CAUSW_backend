package net.causw.app.main.domain.community.report.service.v2.dto;

import java.time.LocalDateTime;

import net.causw.app.main.domain.community.report.repository.projection.ReportedCommentNativeProjection;
import net.causw.app.main.domain.community.report.enums.ReportReason;
import net.causw.app.main.domain.user.account.enums.user.UserState;

public record ReportedCommentSummaryResult(
	String reportId,
	String commentId,
	String commentContent,
	String parentPostTitle,
	String parentPostId,
	String writerName,
	UserState writerState,
	String reportReasonDescription,
	LocalDateTime reportCreatedAt,
	String url
) {
	public static ReportedCommentSummaryResult from(ReportedCommentNativeProjection projection) {
		return new ReportedCommentSummaryResult(
			projection.getReportId(),
			projection.getContentId(),
			projection.getContent(),
			projection.getPostTitle(),
			projection.getPostId(),
			projection.getWriterName(),
			projection.getWriterState(),
			ReportReason.valueOf(projection.getReportReason()).getDescription(),
			projection.getReportCreatedAt(),
			"/board/" + projection.getBoardId() + "/" + projection.getPostId()
		);
	}
}
