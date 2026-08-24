package net.causw.app.main.domain.community.post.api.v2.dto.request;

import java.util.List;

import net.causw.app.main.domain.community.board.entity.BoardGroup;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 목록 조회 요청")
public record PostListCondition(
	@Schema(description = "게시판 ID 목록 (값이 있으면 특정 게시판 조회, null 또는 빈 리스트이고 boardGroup이 있으면 해당 그룹만 조회)", example = "[\"board-uuid-1\", \"board-uuid-2\"]") List<String> boardIds,
	@Schema(description = "게시판 그룹 (소식 = NOTICE, 소통 = COMMUNITY)", example = "NOTICE") BoardGroup boardGroup,
	@Schema(description = "커서 (마지막 게시글의 createdAt)", example = "2026-02-09T12:00:00") String cursor,
	@Schema(description = "조회할 개수", example = "20") Integer size,
	@Schema(description = "검색 키워드", example = "검색어") String keyword) {
}
