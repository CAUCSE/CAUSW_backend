package net.causw.app.main.repository.post.projection;

public interface PostLikeCountProjection {
	String getPostId();
	Long getLikeCount();
}
