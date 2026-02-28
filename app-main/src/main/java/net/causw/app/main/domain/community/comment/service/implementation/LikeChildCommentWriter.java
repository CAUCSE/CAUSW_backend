package net.causw.app.main.domain.community.comment.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.entity.LikeChildComment;
import net.causw.app.main.domain.community.comment.repository.LikeChildCommentRepository;

import lombok.RequiredArgsConstructor;

/**
 * 대댓글 좋아요 엔티티 저장·삭제를 담당합니다.
 */
@Component
@RequiredArgsConstructor
public class LikeChildCommentWriter {

	private final LikeChildCommentRepository likeChildCommentRepository;

	/**
	 * 대댓글 좋아요를 저장합니다.
	 *
	 * @param likeChildComment 저장할 {@link LikeChildComment} 엔티티
	 */
	public void save(LikeChildComment likeChildComment) {
		likeChildCommentRepository.save(likeChildComment);
	}

	/**
	 * 대댓글 좋아요를 취소(삭제)합니다.
	 *
	 * @param childCommentId 좋아요를 취소할 대댓글 ID
	 * @param userId         좋아요를 취소할 유저 ID
	 */
	public void delete(String childCommentId, String userId) {
		likeChildCommentRepository.deleteLikeByChildCommentIdAndUserId(childCommentId, userId);
	}
}
