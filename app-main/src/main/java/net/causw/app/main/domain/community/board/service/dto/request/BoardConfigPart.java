package net.causw.app.main.domain.community.board.service.dto.request;

import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;

import lombok.Builder;

/**
 * 게시판 설정(BoardConfig) 엔티티에 해당하는 필드만 담는 application DTO.
 * 생성/수정 시 재사용한다.
 */
@Builder
public record BoardConfigPart(
	boolean isAnonymous,
	BoardReadScope readScope,
	BoardWriteScope writeScope,
	boolean isNotice,
	BoardVisibility visibility) {
}
