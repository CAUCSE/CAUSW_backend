package net.causw.app.main.domain.community.post.repository.query;

import java.util.Set;

import net.causw.app.main.domain.community.board.entity.BoardReadScope;

/** 게시글 목록 QueryDSL에서 공통 읽기 권한을 적용하기 위한 불변 컨텍스트입니다. */
public record PostReadQueryContext(
	String viewerId,
	boolean systemAdmin,
	Set<BoardReadScope> readableScopes,
	Set<String> blockedWriterIds) {

	public PostReadQueryContext {
		readableScopes = readableScopes == null ? Set.of() : Set.copyOf(readableScopes);
		blockedWriterIds = blockedWriterIds == null ? Set.of() : Set.copyOf(blockedWriterIds);
	}
}
