package net.causw.app.main.domain.community.comment.repository.query;

import com.querydsl.core.annotations.QueryProjection;

public record CommentMetaQueryResult(
	String commentId,
	String parentCommentId,
	Long numLike,
	Boolean isLiked,
	Boolean isBlocked) {

	@QueryProjection
	public CommentMetaQueryResult {
	}
}
