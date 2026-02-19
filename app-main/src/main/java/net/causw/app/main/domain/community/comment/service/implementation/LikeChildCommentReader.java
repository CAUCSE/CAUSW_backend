package net.causw.app.main.domain.community.comment.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.repository.LikeChildCommentRepository;
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

}
