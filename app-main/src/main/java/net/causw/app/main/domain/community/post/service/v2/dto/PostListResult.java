package net.causw.app.main.domain.community.post.service.v2.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostListResult(
	List<PostItem> posts,
	String nextCursor) {

	public static PostListResult of(List<PostItem> posts, String nextCursor) {
		return new PostListResult(posts, nextCursor);
	}

	public record PostItem(
		String postId,
		String content,
		long numComment,
		long numLike,
		long numFavorite,
		boolean isAnonymous,
		String voteId, // 투표 ID (투표가 없으면 null)
		boolean isDeleted,
		String writerNickname, // 익명인 경우 "익명", 아니면 실제 닉네임
		String writerProfileImageUrl, // 익명인 경우 null
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		List<String> postImageUrls) {
		public static PostItem of(
			String postId,
			String content,
			long numComment,
			long numLike,
			long numFavorite,
			boolean isAnonymous,
			String voteId,
			boolean isDeleted,
			String writerNickname,
			String writerProfileImageUrl,
			LocalDateTime createdAt,
			LocalDateTime updatedAt,
			List<String> postImageUrls) {
			return new PostItem(
				postId, content, numComment, numLike, numFavorite,
				isAnonymous, voteId, isDeleted,
				writerNickname, writerProfileImageUrl,
				createdAt, updatedAt, postImageUrls);
		}
	}
}
