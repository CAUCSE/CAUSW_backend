package net.causw.app.main.dto.report;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReportedPostResponseDto {

	@Schema(description = "신고 ID", example = "550e8400-e29b-41d4-a716-446655440000")
	private final String reportId;

	@Schema(description = "게시글 ID", example = "550e8400-e29b-41d4-a716-446655440001")
	private final String postId;

	@Schema(description = "게시글 제목", example = "신고된 게시글 제목")
	private final String postTitle;

	@Schema(description = "작성자 실명", example = "김철수")
	private final String writerName;

	@Schema(description = "작성자 유저 상태", example = "ACTIVE")
	private final String writerState;

	@Schema(description = "신고 사유 설명", example = "낚시/놀람/도배")
	private final String reportReasonDescription;

	@Schema(description = "신고 생성 시간", example = "2024-03-15T10:30:00")
	private final LocalDateTime reportCreatedAt;

	@Schema(description = "게시판 이름", example = "자유게시판")
	private final String boardName;

	@Schema(description = "게시글 URL", example = "/posts/123")
	private final String url;

}