package net.causw.app.main.domain.community.board.service.dto.result;

import lombok.Builder;

/**
 * 게시판 설정 요약(목록 한 항목)을 나타내는 application DTO.
 * Entity → DTO 변환은 {@link net.causw.app.main.domain.community.board.service.dto.mapper.BoardConfigSummaryMapper}를 사용한다.
 */
@Builder
public record BoardConfigSummary(
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
