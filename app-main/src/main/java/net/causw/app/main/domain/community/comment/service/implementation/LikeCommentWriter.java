package net.causw.app.main.domain.community.comment.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.entity.LikeComment;
import net.causw.app.main.domain.community.comment.repository.LikeCommentRepository;

import lombok.RequiredArgsConstructor;

/**
 * 댓글 좋아요 엔티티 저장·삭제를 담당합니다.
 */
@Component
@RequiredArgsConstructor
public class LikeCommentWriter {

	private final LikeCommentRepository likeCommentRepository;

	/**
	 * 댓글 좋아요를 저장합니다.
	 *
	 * @param likeComment 저장할 {@link LikeComment} 엔티티
	 */
	public void save(LikeComment likeComment) {
		likeCommentRepository.save(likeComment);
	}

	/**
	 * 댓글 좋아요를 취소(삭제)합니다.
	 *
	 * @param commentId 좋아요를 취소할 댓글 ID
	 * @param userId    좋아요를 취소할 유저 ID
	 */
	public void delete(String commentId, String userId) {
		likeCommentRepository.deleteLikeByCommentIdAndUserId(commentId, userId);
	}

}
