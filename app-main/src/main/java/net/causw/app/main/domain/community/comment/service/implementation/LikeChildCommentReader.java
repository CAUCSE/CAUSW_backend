package net.causw.app.main.domain.community.comment.service.implementation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.repository.LikeChildCommentRepository;
import net.causw.app.main.domain.community.comment.repository.query.ChildCommentLikeCountDto;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

/**
 * 대댓글 좋아요 집계 데이터 조회를 담당합니다.
 */
@Component
@RequiredArgsConstructor
public class LikeChildCommentReader {

	private final LikeChildCommentRepository likeChildCommentRepository;

	/**
	 * 단일 대댓글의 좋아요 수를 조회합니다.
	 *
	 * @param childComment 조회할 대댓글 엔티티
	 * @return 좋아요 수
	 */
	public Long getNumOfChildCommentLikes(ChildComment childComment) {
		return likeChildCommentRepository.countByChildCommentId(childComment.getId());
	}

	/**
	 * 유저가 특정 대댓글에 좋아요를 눌렀는지 여부를 조회합니다.
	 *
	 * @param user           조회할 유저
	 * @param childCommentId 조회할 대댓글 ID
	 * @return 좋아요 여부
	 */
	public Boolean isChildCommentLiked(User user, String childCommentId) {
		return likeChildCommentRepository.existsByChildCommentIdAndUserId(childCommentId, user.getId());
	}

	/**
	 * 대댓글 ID 목록에 대한 좋아요 수를 배치 조회합니다.
	 *
	 * @param childCommentIds 조회할 대댓글 ID 목록
	 * @return {@code Map<대댓글ID, 좋아요 수>}
	 */
	public Map<String, Long> getChildCommentLikeCounts(List<String> childCommentIds) {
		if (childCommentIds == null || childCommentIds.isEmpty()) {
			return Collections.emptyMap();
		}

		List<ChildCommentLikeCountDto> results = likeChildCommentRepository
			.countLikesByChildCommentIds(childCommentIds);

		return results.stream()
			.collect(Collectors.toMap(
				ChildCommentLikeCountDto::getChildCommentId,
				ChildCommentLikeCountDto::getLikeCount));
	}

	/**
	 * 유저가 좋아요를 누른 대댓글 ID 집합을 배치 조회합니다.
	 *
	 * @param userId          조회할 유저 ID
	 * @param childCommentIds 조회 대상 대댓글 ID 목록
	 * @return 유저가 좋아요를 누른 대댓글 ID 집합
	 */
	public Set<String> getLikedChildCommentIds(String userId, List<String> childCommentIds) {
		if (childCommentIds == null || childCommentIds.isEmpty()) {
			return Collections.emptySet();
		}

		return likeChildCommentRepository.findLikedChildCommentIdsByUserIdAndChildCommentIds(
			userId, childCommentIds);
	}

}
