package net.causw.app.main.domain.community.board.service.v2.dto;

/**
 * 사용자가 쓰기 가능한 게시판 한 건 (id, name).
 */
public record BoardWritableItemResult(
	String id,
	String name) {
}
