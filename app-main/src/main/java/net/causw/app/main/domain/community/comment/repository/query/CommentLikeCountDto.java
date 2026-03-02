package net.causw.app.main.domain.community.comment.repository.query;

import com.querydsl.core.annotations.QueryProjection;

public record CommentLikeCountDto(
	String getCommentId,
	Long getLikeCount) {

	@QueryProjection
	public CommentLikeCountDto {
	}
}
