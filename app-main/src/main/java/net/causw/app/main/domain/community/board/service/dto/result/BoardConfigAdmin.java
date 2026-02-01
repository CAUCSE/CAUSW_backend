package net.causw.app.main.domain.community.board.service.dto.result;

/**
 * 게시판 관리자 한 명을 나타내는 application DTO.
 * Entity → DTO 변환은 {@link net.causw.app.main.domain.community.board.service.dto.mapper.BoardConfigAdminMapper}를 사용한다.
 */
public record BoardConfigAdmin(
	String id,
	String adminEmail,
	String adminName) {
}
