package net.causw.app.main.domain.community.comment.service.implementation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.service.dto.CommentMeta;
import net.causw.app.main.domain.notification.notification.service.implementation.UserCommentSubscribeReader;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

/**
 * 댓글 렌더링에 필요한 집계 데이터를 조회합니다.
 *
 * <p>목록 조회 시에는 {@link #fetch}로 배치 쿼리를 실행하여 {@code Map<commentId, CommentMeta>}를 반환하고,
 * 단건 작업(수정·삭제) 시에는 {@link #fetchForComment}로 단건 조회를 수행합니다.</p>
 */
@Component
@RequiredArgsConstructor
public class CommentMetaReader {

	private final LikeCommentReader likeCommentReader;
	private final LikeChildCommentReader likeChildCommentReader;
	private final UserCommentSubscribeReader userCommentSubscribeReader;

	/**
	 * 댓글 목록 렌더링에 필요한 집계 데이터를 배치로 조회합니다.
	 *
	 * @param userId         현재 조회하는 유저의 ID
	 * @param blockedUserIds 현재 유저가 차단한 유저 ID 집합
	 * @param comments       렌더링할 댓글 목록
	 * @return 댓글 ID → CommentMeta 매핑
	 */
	public Map<String, CommentMeta> fetch(String userId, Set<String> blockedUserIds, List<Comment> comments) {
		List<String> commentIds = comments.stream().map(Comment::getId).toList();
		List<String> childCommentIds = comments.stream()
			.flatMap(c -> c.getChildCommentList().stream())
			.map(ChildComment::getId)
			.toList();

		Map<String, Long> commentLikeCounts = likeCommentReader.getCommentLikeCounts(commentIds);
		Set<String> likedCommentIds = likeCommentReader.getLikedCommentIds(userId, commentIds);
		Set<String> subscribedCommentIds = userCommentSubscribeReader.getSubscribedCommentIds(userId, commentIds);

		Map<String, Long> childCommentLikeCounts = likeChildCommentReader.getChildCommentLikeCounts(childCommentIds);
		Set<String> likedChildCommentIds = likeChildCommentReader.getLikedChildCommentIds(userId, childCommentIds);

		Map<String, CommentMeta> result = new HashMap<>();
		for (Comment comment : comments) {
			String cId = comment.getId();

			Map<String, Long> myChildLikeCounts = new HashMap<>();
			Set<String> myLikedChildIds = new HashSet<>();
			Set<String> myBlockedChildIds = new HashSet<>();

			for (ChildComment child : comment.getChildCommentList()) {
				String childId = child.getId();
				Long likeCount = childCommentLikeCounts.get(childId);
				if (likeCount != null) {
					myChildLikeCounts.put(childId, likeCount);
				}
				if (likedChildCommentIds.contains(childId)) {
					myLikedChildIds.add(childId);
				}
				if (blockedUserIds.contains(child.getWriter().getId())) {
					myBlockedChildIds.add(childId);
				}
			}

			result.put(cId, new CommentMeta(
				commentLikeCounts.getOrDefault(cId, 0L),
				likedCommentIds.contains(cId),
				subscribedCommentIds.contains(cId),
				blockedUserIds.contains(comment.getWriter().getId()),
				Map.copyOf(myChildLikeCounts),
				Set.copyOf(myLikedChildIds),
				Set.copyOf(myBlockedChildIds)));
		}
		return result;
	}

	/**
	 * 단일 댓글 렌더링에 필요한 집계 데이터를 조회합니다.
	 * 단건 owner 작업(수정/삭제)이므로 isBlocked=false, blockedChildIds=Set.of()
	 *
	 * @param user    현재 조회하는 유저
	 * @param comment 렌더링할 댓글
	 * @return CommentMeta
	 */
	public CommentMeta fetchForComment(User user, Comment comment) {
		long numLike = likeCommentReader.getNumOfCommentLikes(comment.getId());
		boolean isLiked = likeCommentReader.isCommentLiked(user, comment.getId());
		boolean isSubscribed = userCommentSubscribeReader.isCommentSubscribed(user, comment);

		List<String> childIds = comment.getChildCommentList().stream().map(ChildComment::getId).toList();
		Map<String, Long> childLikeCounts = likeChildCommentReader.getChildCommentLikeCounts(childIds);
		Set<String> likedChildIds = likeChildCommentReader.getLikedChildCommentIds(user.getId(), childIds);

		return new CommentMeta(
			numLike,
			isLiked,
			isSubscribed,
			false,
			childLikeCounts,
			likedChildIds,
			Set.of());
	}

}
