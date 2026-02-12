package net.causw.app.main.domain.community.post.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 목록 조회 요청")
public record PostListCondition(
	@Schema(description = "게시판 ID (null이면 전체 게시판)", example = " board-uuid") String boardId,
	@Schema(description = "커서 (마지막 게시글의 createdAt)", example = "2026-02-09T12:00:00") String cursor,
	@Schema(description = "조회할 개수", example = "20") Integer size,
	@Schema(description = "검색 키워드", example = "검색어") String keyword) {
}
