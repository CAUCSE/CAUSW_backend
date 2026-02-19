package net.causw.app.main.domain.community.comment.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.entity.LikeComment;
import net.causw.app.main.domain.community.comment.repository.LikeCommentRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LikeCommentWriter {

	private final LikeCommentRepository likeCommentRepository;

	public void save(LikeComment likeComment) {
		likeCommentRepository.save(likeComment);
	}

	public void delete(String commentId, String userId) {
		likeCommentRepository.deleteLikeByCommentIdAndUserId(commentId, userId);
	}

}
