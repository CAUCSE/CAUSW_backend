package net.causw.app.main.domain.community.comment.repository.query;

public record CommentLikeCountDto(
	String getCommentId,
	Long getLikeCount) {
}
