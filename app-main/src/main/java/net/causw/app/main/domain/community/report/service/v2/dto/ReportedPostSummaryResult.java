package net.causw.app.main.domain.community.report.service.v2.dto;

import java.time.LocalDateTime;

import net.causw.app.main.domain.community.report.api.v1.dto.ReportedPostNativeProjection;
import net.causw.app.main.domain.community.report.enums.ReportReason;
import net.causw.app.main.domain.user.account.enums.user.UserState;

public record ReportedPostSummaryResult(
	String reportId,
	String postId,
	String postTitle,
	String writerName,
	UserState writerState,
	String reportReasonDescription,
	LocalDateTime reportCreatedAt,
	String boardName,
	String url
) {
	public static ReportedPostSummaryResult from(ReportedPostNativeProjection projection) {
		return new ReportedPostSummaryResult(
			projection.getReportId(),
			projection.getPostId(),
			projection.getPostTitle(),
			projection.getWriterName(),
			projection.getWriterState(),
			ReportReason.valueOf(projection.getReportReason()).getDescription(),
			projection.getReportCreatedAt(),
			projection.getBoardName(),
			"/board/" + projection.getBoardId() + "/" + projection.getPostId()
		);
	}
}
