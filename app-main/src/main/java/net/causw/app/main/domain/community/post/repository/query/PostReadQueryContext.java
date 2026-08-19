package net.causw.app.main.domain.community.post.repository.query;

import java.util.HashSet;
import java.util.Set;

import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.common.service.CommunityPermissionPolicy;
import net.causw.app.main.domain.user.account.entity.user.User;

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

	public static PostReadQueryContext from(User viewer, Set<String> blockedWriterIds) {
		return new PostReadQueryContext(
			viewer.getId(),
			CommunityPermissionPolicy.isSystemAdmin(viewer),
			new HashSet<>(BoardReadScope.fromAcademicStatus(viewer.getAcademicStatus())),
			blockedWriterIds);
	}
}
