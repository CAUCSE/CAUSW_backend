package net.causw.app.main.domain.community.comment.repository.query;

public record ChildCommentLikeCountDto(
	String getChildCommentId,
	Long getLikeCount) {
}
