package net.causw.app.main.domain.community.board.api.v2.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "쓰기 가능한 게시판 목록 응답")
public record BoardWritableListResponse(
	@Schema(description = "쓰기 가능한 게시판 목록 (표시 순서에 따라 정렬)") List<BoardWritableItemResponse> boards) {
}
