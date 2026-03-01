package net.causw.app.main.domain.community.board.api.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "읽기 가능한 게시판 한 건")
public record BoardReadableItemResponse(
	@Schema(description = "게시판 ID") String id,
	@Schema(description = "게시판 이름") String name
) {
}
