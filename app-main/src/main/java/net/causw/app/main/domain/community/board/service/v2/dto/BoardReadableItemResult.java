package net.causw.app.main.domain.community.board.service.v2.dto;

/**
 * 사용자가 읽기 가능한 게시판 한 건 (id, name).
 */
public record BoardReadableItemResult(
	String id,
	String name
) {
}
