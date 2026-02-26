package net.causw.app.main.domain.community.comment.service.implementation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.repository.LikeChildCommentRepository;
import net.causw.app.main.domain.community.comment.service.dto.ChildCommentLikeCountDto;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LikeChildCommentReader {

	private final LikeChildCommentRepository likeChildCommentRepository;

	public Long getNumOfChildCommentLikes(ChildComment childComment) {
		return likeChildCommentRepository.countByChildCommentId(childComment.getId());
	}

	public Boolean isChildCommentLiked(User user, String childCommentId) {
		return likeChildCommentRepository.existsByChildCommentIdAndUserId(childCommentId, user.getId());
	}

	/**
	 * @param childCommentIds 자식 댓글 ID 리스트
	 * @return Map<자식댓글ID, 좋아요 수>
	 */
	public Map<String, Long> getChildCommentLikeCounts(List<String> childCommentIds) {
		if (childCommentIds == null || childCommentIds.isEmpty()) {
			return Collections.emptyMap();
		}

		List<ChildCommentLikeCountDto> results = likeChildCommentRepository
			.countLikesByChildCommentIds(childCommentIds);

		// List를 Map으로 변환
		return results.stream()
			.collect(Collectors.toMap(
				ChildCommentLikeCountDto::getChildCommentId,
				ChildCommentLikeCountDto::getLikeCount));
	}

	/**
	 * @param userId 현재 조회하는 유저의 ID
	 * @param childCommentIds 대댓글 ID 리스트
	 * @return Set<대댓글ID> (유저가 좋아요를 누른 대댓글들의 ID 집합)
	 */
	public Set<String> getLikedChildCommentIds(String userId, List<String> childCommentIds) {
		if (childCommentIds == null || childCommentIds.isEmpty()) {
			return Collections.emptySet();
		}

		return likeChildCommentRepository.findLikedChildCommentIdsByUserIdAndChildCommentIds(
			userId, childCommentIds);
	}

}
