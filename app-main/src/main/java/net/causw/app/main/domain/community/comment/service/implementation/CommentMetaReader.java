package net.causw.app.main.domain.community.comment.service.implementation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.repository.CommentMetaQueryRepository;
import net.causw.app.main.domain.community.comment.repository.query.CommentMetaQueryResult;
import net.causw.app.main.domain.community.comment.service.dto.CommentMeta;
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

	private final CommentMetaQueryRepository commentMetaQueryRepository;

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
			.map(Comment::getId)
			.toList();
		Map<String, CommentMetaQueryResult> queryResultMap = fetchQueryResultMap(
			userId,
			blockedUserIds,
			Stream.concat(commentIds.stream(), childCommentIds.stream()).toList());

		Map<String, CommentMeta> result = new HashMap<>();
		for (Comment comment : comments) {
			result.put(comment.getId(), buildMeta(comment.getId(), comment.getChildCommentList(), queryResultMap));
		}
		return result;
	}

	/**
	 * 단일 댓글 렌더링에 필요한 집계 데이터를 조회합니다.
	 *
	 * @param user    현재 조회하는 유저
	 * @param comment 렌더링할 댓글
	 * @param blockedUserIds 현재 유저가 차단한 유저 ID 집합
	 * @return CommentMeta
	 */
	public CommentMeta fetchForComment(User user, Comment comment, Set<String> blockedUserIds) {
		List<String> childIds = comment.getChildCommentList().stream().map(Comment::getId).toList();
		Map<String, CommentMetaQueryResult> queryResultMap = fetchQueryResultMap(
			user.getId(),
			blockedUserIds,
			Stream.concat(Stream.of(comment.getId()), childIds.stream()).toList());
		return buildMeta(comment.getId(), comment.getChildCommentList(), queryResultMap);
	}

	private Map<String, CommentMetaQueryResult> fetchQueryResultMap(
		String userId,
		Set<String> blockedUserIds,
		List<String> commentIds) {
		return commentMetaQueryRepository.findCommentMetaByCommentIds(userId, blockedUserIds, commentIds).stream()
			.collect(Collectors.toMap(CommentMetaQueryResult::commentId, result -> result));
	}

	private CommentMeta buildMeta(
		String commentId,
		List<Comment> childComments,
		Map<String, CommentMetaQueryResult> queryResultMap) {
		CommentMetaQueryResult commentMeta = queryResultMap.get(commentId);
		Map<String, Long> childLikeCounts = new HashMap<>();
		Set<String> likedChildIds = new HashSet<>();
		Set<String> blockedChildIds = new HashSet<>();

		for (Comment child : childComments) {
			String childId = child.getId();
			CommentMetaQueryResult childMeta = queryResultMap.get(childId);
			if (childMeta == null) {
				continue;
			}
			childLikeCounts.put(childId, childMeta.numLike());
			if (Boolean.TRUE.equals(childMeta.isLiked())) {
				likedChildIds.add(childId);
			}
			if (Boolean.TRUE.equals(childMeta.isBlocked())) {
				blockedChildIds.add(childId);
			}
		}

		return new CommentMeta(
			commentMeta != null ? commentMeta.numLike() : 0L,
			commentMeta != null && Boolean.TRUE.equals(commentMeta.isLiked()),
			commentMeta != null && Boolean.TRUE.equals(commentMeta.isBlocked()),
			Map.copyOf(childLikeCounts),
			Set.copyOf(likedChildIds),
			Set.copyOf(blockedChildIds));
	}

}
