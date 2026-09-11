package net.causw.app.main.domain.community.post.api.v2.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "성격 미분류 게시글 응답")
public record UncategorizedPostResponse(
	@Schema(description = "게시글 ID") String postId,

	@Schema(description = "게시글 제목") String title,

	@Schema(description = "게시판 이름") String boardName,

	@Schema(description = "작성 시각") LocalDateTime createdAt) {
}
