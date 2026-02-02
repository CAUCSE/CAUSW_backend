package net.causw.app.main.domain.community.board.api.v2.dto.response;

import java.util.List;

public record BoardConfigListResponse(
	List<BoardAdminResponse> boards) {
	public record BoardAdminResponse(
		Long no,
		String boardId,
		String name,
		String description,
		Boolean isAnonymous,
		String readScope,
		String writeScope,
		Boolean isNotice,
		String visibility,
		Integer displayOrder) {
	}
}
