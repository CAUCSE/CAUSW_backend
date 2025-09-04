package net.causw.app.main.dto.report;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportedCommentResponseDto {

	@Schema(description = "신고 ID", example = "550e8400-e29b-41d4-a716-446655440000")
	private final String reportId;

	@Schema(description = "댓글 ID", example = "550e8400-e29b-41d4-a716-446655440002")
	private final String commentId;

	@Schema(description = "댓글 내용", example = "신고된 댓글 내용")
	private final String commentContent;

	@Schema(description = "댓글이 달린 게시글 제목", example = "댓글이 달린 게시글 제목")
	private final String parentPostTitle;

	@Schema(description = "댓글이 달린 게시글 ID", example = "550e8400-e29b-41d4-a716-446655440001")
	private final String parentPostId;

	@Schema(description = "작성자 실명", example = "김철수")
	private final String writerName;

	@Schema(description = "작성자 유저 상태", example = "ACTIVE")
	private final String writerState;

	@Schema(description = "신고 사유 설명", example = "욕설/비하")
	private final String reportReasonDescription;

	@Schema(description = "신고 생성 시간", example = "2024-03-15T10:30:00")
	private final LocalDateTime reportCreatedAt;

	@Schema(description = "게시글 URL", example = "/board/board-id/post-id")
	private final String url;
}