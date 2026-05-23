package net.causw.app.main.domain.community.post.service.v2.dto;

import java.time.LocalDateTime;
import java.util.List;

import net.causw.app.main.shared.dto.ProfileImageDto;

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
		boolean isCrawled,
		String writerNickname, // 익명인 경우 "익명", 아니면 실제 닉네임
		ProfileImageDto writerProfileImage, // 익명/차단/추방/탈퇴 시 GHOST
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		List<String> postImageUrls,
		String boardId,
		String boardName,
		boolean isPostLike,
		boolean isOwner,
		boolean isOfficial) {
		public static PostItem of(
			String postId,
			String content,
			long numComment,
			long numLike,
			long numFavorite,
			boolean isAnonymous,
			String voteId,
			boolean isDeleted,
			boolean isCrawled,
			String writerNickname,
			ProfileImageDto writerProfileImage,
			LocalDateTime createdAt,
			LocalDateTime updatedAt,
			List<String> postImageUrls,
			String boardId,
			String boardName,
			boolean isPostLike,
			boolean isOwner,
			boolean isOfficial) {
			return new PostItem(
				postId, content, numComment, numLike, numFavorite,
				isAnonymous, voteId, isDeleted, isCrawled,
				writerNickname, writerProfileImage,
				createdAt, updatedAt, postImageUrls,
				boardId, boardName, isPostLike, isOwner, isOfficial);
		}
	}
}
