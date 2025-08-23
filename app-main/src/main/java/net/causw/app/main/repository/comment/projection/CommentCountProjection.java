package net.causw.app.main.repository.comment.projection;

public interface CommentCountProjection {
	String getPostId();
	Long getCommentCount();
}