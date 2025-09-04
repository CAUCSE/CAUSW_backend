package net.causw.app.main.dto.report;

import java.time.LocalDateTime;

import net.causw.app.main.domain.model.enums.user.UserState;

/**
 * Native Query 결과를 받기 위한 Projection 인터페이스
 * 댓글과 대댓글 신고를 UNION ALL로 합쳐서 조회할 때 사용
 */
public interface ReportedCommentNativeProjection {
	String getReportId();

	String getContentId();

	String getContent();

	String getPostTitle();

	String getPostId();

	String getBoardId();

	String getWriterName();

	UserState getWriterState();

	String getReportReason();

	LocalDateTime getReportCreatedAt();
}