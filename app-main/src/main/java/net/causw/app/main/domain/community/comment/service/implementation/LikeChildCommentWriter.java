package net.causw.app.main.domain.community.comment.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.entity.LikeChildComment;
import net.causw.app.main.domain.community.comment.repository.LikeChildCommentRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LikeChildCommentWriter {
	private final LikeChildCommentRepository likeChildCommentRepository;

	public void save(LikeChildComment likeChildComment) {
		likeChildCommentRepository.save(likeChildComment);
	}

	public void delete(String childCommentId, String userId) {
		likeChildCommentRepository.deleteLikeByChildCommentIdAndUserId(childCommentId, userId);
	}
}
