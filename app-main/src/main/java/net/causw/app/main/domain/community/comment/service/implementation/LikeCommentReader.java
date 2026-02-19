package net.causw.app.main.domain.community.comment.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.repository.LikeCommentRepository;
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

}
