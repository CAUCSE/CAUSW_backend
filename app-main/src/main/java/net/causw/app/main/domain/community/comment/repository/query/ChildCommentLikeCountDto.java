package net.causw.app.main.domain.community.comment.repository.query;

import com.querydsl.core.annotations.QueryProjection;

public record ChildCommentLikeCountDto(
	String getChildCommentId,
	Long getLikeCount) {

	@QueryProjection
	public ChildCommentLikeCountDto {
	}
}
