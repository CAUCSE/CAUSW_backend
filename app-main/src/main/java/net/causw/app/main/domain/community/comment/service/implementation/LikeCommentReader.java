package net.causw.app.main.domain.community.comment.service.implementation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.repository.LikeCommentRepository;
import net.causw.app.main.domain.community.comment.service.dto.CommentLikeCountDto;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LikeCommentReader {

	private final LikeCommentRepository likeCommentRepository;

	public Long getNumOfCommentLikes(String commentId) {
		return likeCommentRepository.countByCommentId(commentId);
	}

	public Boolean isCommentLiked(User user, String commentId) {
		return likeCommentRepository.existsByCommentIdAndUserId(commentId, user.getId());
	}

	/**
	 * @param commentIds 부모 댓글 ID 리스트
	 * @return Map<댓글ID, 좋아요 수>
	 */
	public Map<String, Long> getCommentLikeCounts(List<String> commentIds) {
		if (commentIds == null || commentIds.isEmpty()) {
			return Collections.emptyMap();
		}

		List<CommentLikeCountDto> results = likeCommentRepository.countLikesByCommentIds(commentIds);

		// Map으로 변환
		return results.stream()
			.collect(Collectors.toMap(
				CommentLikeCountDto::getCommentId,
				CommentLikeCountDto::getLikeCount));
	}

	/**
	 * @param userId 현재 조회하는 유저의 ID
	 * @param commentIds 부모 댓글 ID 리스트
	 * @return Set<댓글ID>
	 */
	public Set<String> getLikedCommentIds(String userId, List<String> commentIds) {
		if (commentIds == null || commentIds.isEmpty()) {
			return Collections.emptySet();
		}
		return likeCommentRepository.findLikedCommentIdsByUserIdAndCommentIds(userId, commentIds);
	}

}
