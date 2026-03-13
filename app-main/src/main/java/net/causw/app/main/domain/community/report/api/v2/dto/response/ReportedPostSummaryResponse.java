package net.causw.app.main.domain.community.report.api.v2.dto.response;

import java.time.LocalDateTime;

import net.causw.app.main.domain.user.account.enums.user.UserState;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 신고 게시글 목록 아이템")
public record ReportedPostSummaryResponse(
	@Schema(description = "신고 ID", example = "550e8400-e29b-41d4-a716-446655440000") String reportId,

	@Schema(description = "게시글 ID", example = "550e8400-e29b-41d4-a716-446655440001") String postId,

	@Schema(description = "게시글 제목", example = "신고된 게시글 제목") String postTitle,

	@Schema(description = "작성자 실명", example = "김철수") String writerName,

	@Schema(description = "작성자 유저 상태", example = "ACTIVE") UserState writerState,

	@Schema(description = "신고 사유 설명", example = "낚시/놀람/도배") String reportReasonDescription,

	@Schema(description = "신고 생성 시간", example = "2024-03-15T10:30:00") LocalDateTime reportCreatedAt,

	@Schema(description = "게시판 이름", example = "자유게시판") String boardName,

	@Schema(description = "게시글 URL", example = "/board/board-id/post-id") String url) {
}
