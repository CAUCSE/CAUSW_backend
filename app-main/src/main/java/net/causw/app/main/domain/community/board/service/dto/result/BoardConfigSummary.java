package net.causw.app.main.domain.community.board.service.dto.result;

import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;

import lombok.Builder;

/**
 * 게시판 설정 요약(목록 한 항목)을 나타내는 application DTO.
 * Entity → DTO 변환은 {@link net.causw.app.main.domain.community.board.service.dto.mapper.BoardConfigSummaryMapper}를 사용한다.
 * Dto -> Response 변환은 {@link net.causw.app.main.domain.community.board.api.v2.mapper.BoardAdminListMapper}를 사용한다.
 */
@Builder
public record BoardConfigSummary(
	Long no,
	String boardId,
	String name,
	String description,
	Boolean isAnonymous,
	BoardReadScope readScope,
	BoardWriteScope writeScope,
	Boolean isNotice,
	BoardVisibility visibility,
	Integer displayOrder) {
}
