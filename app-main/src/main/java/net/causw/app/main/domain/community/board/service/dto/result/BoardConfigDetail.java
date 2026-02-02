package net.causw.app.main.domain.community.board.service.dto.result;

import java.util.List;

import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;

import lombok.Builder;

/**
 * 게시판 설정 상세(편집/상세 조회용)를 나타내는 application DTO.
 * Entity → DTO 변환은 {@link net.causw.app.main.domain.community.board.service.dto.mapper.BoardConfigDetailMapper}를 사용한다.
 * Dto -> Response 변환은 {@link net.causw.app.main.domain.community.board.api.v2.mapper.BoardAdminMapper}를 사용한다.
 */
@Builder
public record BoardConfigDetail(
	String boardId,
	String name,
	String description,
	Boolean isAnonymous,
	BoardReadScope readScope,
	BoardWriteScope writeScope,
	Boolean isNotice,
	BoardVisibility visibility,
	Integer displayOrder,
	List<BoardConfigAdmin> admins) {
}
