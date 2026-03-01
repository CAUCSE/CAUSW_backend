package net.causw.app.main.domain.community.board.api.v2.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "읽기 가능한 게시판 목록 응답")
public record BoardReadableListResponse(
	@Schema(description = "읽기 가능한 게시판 목록 (표시 순서)") List<BoardReadableItemResponse> boards
) {
}
