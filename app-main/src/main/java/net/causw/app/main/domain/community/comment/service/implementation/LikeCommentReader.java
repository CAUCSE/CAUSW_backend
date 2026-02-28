package net.causw.app.main.domain.community.comment.service.implementation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.repository.LikeCommentRepository;
import net.causw.app.main.domain.community.comment.repository.query.CommentLikeCountDto;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

/**
 * 댓글 좋아요 집계 데이터 조회를 담당합니다.
 */
@Component
@RequiredArgsConstructor
public class LikeCommentReader {

	private final LikeCommentRepository likeCommentRepository;

	/**
	 * 단일 댓글의 좋아요 수를 조회합니다.
	 *
	 * @param commentId 조회할 댓글 ID
	 * @return 좋아요 수
	 */
	public Long getNumOfCommentLikes(String commentId) {
		return likeCommentRepository.countByCommentId(commentId);
	}

	/**
	 * 유저가 특정 댓글에 좋아요를 눌렀는지 여부를 조회합니다.
	 *
	 * @param user      조회할 유저
	 * @param commentId 조회할 댓글 ID
	 * @return 좋아요 여부
	 */
	public Boolean isCommentLiked(User user, String commentId) {
		return likeCommentRepository.existsByCommentIdAndUserId(commentId, user.getId());
	}

	/**
	 * 댓글 ID 목록에 대한 좋아요 수를 배치 조회합니다.
	 *
	 * @param commentIds 조회할 댓글 ID 목록
	 * @return {@code Map<댓글ID, 좋아요 수>}
	 */
	public Map<String, Long> getCommentLikeCounts(List<String> commentIds) {
		if (commentIds == null || commentIds.isEmpty()) {
			return Collections.emptyMap();
		}

		List<CommentLikeCountDto> results = likeCommentRepository.countLikesByCommentIds(commentIds);

		return results.stream()
			.collect(Collectors.toMap(
				CommentLikeCountDto::getCommentId,
				CommentLikeCountDto::getLikeCount));
	}

	/**
	 * 유저가 좋아요를 누른 댓글 ID 집합을 배치 조회합니다.
	 *
	 * @param userId     조회할 유저 ID
	 * @param commentIds 조회 대상 댓글 ID 목록
	 * @return 유저가 좋아요를 누른 댓글 ID 집합
	 */
	public Set<String> getLikedCommentIds(String userId, List<String> commentIds) {
		if (commentIds == null || commentIds.isEmpty()) {
			return Collections.emptySet();
		}
		return likeCommentRepository.findLikedCommentIdsByUserIdAndCommentIds(userId, commentIds);
	}

}
