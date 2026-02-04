package net.causw.app.main.domain.community.board.service.dto.request;

import lombok.Builder;

/**
 * 게시판(Board) 엔티티에 해당하는 필드만 담는 application DTO.
 * 생성/수정 시 재사용한다.
 */
@Builder
public record BoardPart(
	String name,
	String description) {
}
